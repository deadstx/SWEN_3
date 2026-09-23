package org.example.swen3backend.service;

import lombok.RequiredArgsConstructor;
import org.example.swen3backend.dto.DocumentResponse;
import org.example.swen3backend.dto.UpdateDocumentRequest;
import org.example.swen3backend.entity.DocumentEntity;
import org.example.swen3backend.mapper.DocumentMapper;
import org.example.swen3backend.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;

    public DocumentResponse upload(String originalFilename, byte[] content) {
        String name = normalizeFilename(originalFilename);
        validateContent(content);

        DocumentEntity document = new DocumentEntity(
                name, content, content.length, Instant.now()
        );
        DocumentEntity saved = documentRepository.save(document);

        return documentMapper.toDto(saved);
    }

    public List<DocumentResponse> getAll() {
        return documentMapper.toDtoList(documentRepository.findAll());
    }

    public DocumentResponse getById(long id) {
        return documentMapper.toDto(findDocument(id));
    }

    public DocumentResponse update(long id, UpdateDocumentRequest request) {
        DocumentEntity document = findDocument(id);
        if (request.name() == null && request.tags() == null) {
            throw new InvalidDocumentException("Name oder Tags müssen angegeben werden.");
        }

        String name = request.name() != null
                ? validateAndStrip(request.name(), "Der Name")
                : null;

        List<String> tags = request.tags() != null
                ? request.tags().stream()
                .map(tag -> validateAndStrip(tag, "Jeder Tag"))
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
        documentRepository.delete(findDocument(id));
    }

    private DocumentEntity findDocument(long id) {
        if (id <= 0) {
            throw new InvalidDocumentException("Die Dokument-ID muss positiv sein.");
        }
        return documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));
    }

    private String validateAndStrip(String value, String fieldLabel) {
        if (value == null) {
            throw new InvalidDocumentException(fieldLabel + " darf nicht null sein.");
        }
        String stripped = value.strip();
        if (stripped.isBlank() || stripped.length() > 255) {
            throw new InvalidDocumentException(
                    fieldLabel + " muss zwischen 1 und 255 Zeichen enthalten."
            );
        }
        return stripped;
    }

    private String normalizeFilename(String originalFilename) {
        if (originalFilename == null) {
            throw new InvalidDocumentException("Ein Dateiname ist erforderlich.");
        }

        String normalized = originalFilename.replace('\\', '/');
        String name = normalized.substring(normalized.lastIndexOf('/') + 1);

        return validateAndStrip(name, "Der Dateiname");
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