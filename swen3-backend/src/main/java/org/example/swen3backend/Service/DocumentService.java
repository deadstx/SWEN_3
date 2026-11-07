package org.example.swen3backend.service;

import lombok.RequiredArgsConstructor;
import org.example.swen3backend.dto.DocumentResponse;
import org.example.swen3backend.dto.UpdateDocumentRequest;
import org.example.swen3backend.entity.DocumentEntity;
import org.example.swen3backend.mapper.DocumentMapper;
import org.example.swen3backend.search.DocumentSearch;
import org.example.swen3backend.repository.DocumentRepository;
import org.example.swen3backend.repository.DocumentCollectionRepository;
import org.example.swen3backend.validation.FieldValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.swen3backend.messaging.OcrMessage;
import org.example.swen3backend.messaging.OcrMessageProducer;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.example.swen3backend.storage.DocumentStorage;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;
    private final DocumentCollectionRepository collectionRepository;
    private final FieldValidator fieldValidator;
    private final OcrMessageProducer ocrMessageProducer;
    private final DocumentStorage documentStorage;
    private final DocumentSearch documentSearch;

    public DocumentResponse upload(String originalFilename, byte[] content) {
        String name = normalizeFilename(originalFilename);
        validateContent(content);

        String objectKey = "documents/" + UUID.randomUUID() + ".pdf";
        documentStorage.upload(objectKey, content);
        // RustFS is outside the SQL transaction: clean up if metadata or messaging fails.
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_ROLLED_BACK) {
                        try {
                            documentStorage.delete(objectKey);
                        } catch (RuntimeException exception) {
                            log.error("Could not clean up rolled-back upload {}", objectKey, exception);
                        }
                    }
                }
            });
        }

        DocumentEntity document = new DocumentEntity(name, objectKey, content.length, Instant.now());
        DocumentEntity saved = documentRepository.save(document);
        log.info("Document saved id={}, name={}, size={} bytes", saved.getId(), saved.getName(), content.length);

        ocrMessageProducer.sendOcrRequest(new OcrMessage(saved.getId(), saved.getName(),
                documentStorage.getBucket(), objectKey));

        return documentMapper.toDto(saved);
    }

    public List<DocumentResponse> getAll() {
        return documentMapper.toDtoList(documentRepository.findAll());
    }

    public DocumentResponse getById(long id) {
        return documentMapper.toDto(findDocument(id));
    }

    public List<DocumentResponse> search(String query) {
        if (query == null || query.isBlank()) {
            return getAll();
        }
        return documentMapper.toDtoList(documentRepository.findAllById(documentSearch.search(query.strip())));
    }

    public byte[] downloadById(long id) {
        DocumentEntity document = findDocument(id);
        if (document.getObjectKey() == null) {
            throw new DocumentNotFoundException(id);
        }
        return documentStorage.download(document.getObjectKey());
    }

    public DocumentResponse update(long id, UpdateDocumentRequest request) {
        DocumentEntity document = findDocument(id);
        if (request.name() == null && request.tags() == null) {
            throw new InvalidDocumentException("Name oder Tags müssen angegeben werden.");
        }

        String name = request.name() != null
                ? fieldValidator.validateAndStrip(request.name(), "Der Name")
                : null;

        List<String> tags = request.tags() != null
                ? request.tags().stream()
                .map(tag -> fieldValidator.validateAndStrip(tag, "Jeder Tag"))
                .toList()
                : null;

        if (name != null) {
            document.setName(name);
        }
        if (tags != null) {
            document.getTags().clear();
            document.getTags().addAll(tags);
        }
        return documentMapper.toDto(documentRepository.save(document));
    }

    public void delete(long id) {
        DocumentEntity document = findDocument(id);
        collectionRepository.removeDocumentReferences(id);
        if (document.getObjectKey() != null) {
            documentStorage.delete(document.getObjectKey());
        }
        documentSearch.delete(id); // auch aus elastic search löschen wichtig
        documentRepository.delete(document);
    }

    private DocumentEntity findDocument(long id) {
        if (id <= 0) {
            throw new InvalidDocumentException("Die Dokument-ID muss positiv sein.");
        }
        return documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));
    }

    private String normalizeFilename(String originalFilename) {
        if (originalFilename == null) {
            throw new InvalidDocumentException("Ein Dateiname ist erforderlich.");
        }

        String normalized = originalFilename.replace('\\', '/');
        String name = normalized.substring(normalized.lastIndexOf('/') + 1);

        return fieldValidator.validateAndStrip(name, "Der Dateiname");
    }

    private void validateContent(byte[] content) {
        if (content == null || content.length == 0) {
            throw new InvalidDocumentException("Die Datei darf nicht leer sein.");
        }

        if (content.length < 5 ||
                !"%PDF-".equals(new String(
                        content, 0, 5, StandardCharsets.US_ASCII))) {
            throw new InvalidDocumentException("Es sind nur PDF-Dateien erlaubt.");
        }
    }
}
