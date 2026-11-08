package at.technikum.swen3backend.Service;

import at.technikum.swen3backend.exception.DocumentNotFoundException;
import at.technikum.swen3backend.exception.InvalidDocumentException;
import at.technikum.swen3backend.exception.CollectionNotFoundException;

import lombok.RequiredArgsConstructor;
import at.technikum.swen3backend.DTO.DocumentCollectionResponse;
import at.technikum.swen3backend.Entity.DocumentCollectionEntity;
import at.technikum.swen3backend.Entity.DocumentEntity;
import at.technikum.swen3backend.Mapper.DocumentCollectionMapper;
import at.technikum.swen3backend.Repository.DocumentCollectionRepository;
import at.technikum.swen3backend.Repository.DocumentRepository;
import at.technikum.swen3backend.Validation.FieldValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentCollectionService {

    private final DocumentCollectionRepository collectionRepository;
    private final DocumentRepository documentRepository;
    private final DocumentCollectionMapper collectionMapper;
    private final FieldValidator fieldValidator;

    public DocumentCollectionResponse create(String name) {
        String validatedName = fieldValidator.validateAndStrip(name, "Der Name");
        return collectionMapper.toDto(
                collectionRepository.save(new DocumentCollectionEntity(validatedName))
        );
    }

    public List<DocumentCollectionResponse> getAll() {
        return collectionMapper.toDtoList(collectionRepository.findAll());
    }

    public DocumentCollectionResponse getById(long id) {
        return collectionMapper.toDto(findCollection(id));
    }

    public void delete(long id) {
        collectionRepository.delete(findCollection(id));
    }

    public void addDocument(long collectionId, long documentId) {
        DocumentCollectionEntity collection = findCollection(collectionId);
        DocumentEntity document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
        collection.getDocuments().add(document);
        collectionRepository.save(collection);
    }

    public void removeDocument(long collectionId, long documentId) {
        DocumentCollectionEntity collection = findCollection(collectionId);
        collection.getDocuments().removeIf(document -> document.getId().equals(documentId));
        collectionRepository.save(collection);
    }

    private DocumentCollectionEntity findCollection(long id) {
        if (id <= 0) {
            throw new InvalidDocumentException("Die Sammlungs-ID muss positiv sein.");
        }
        return collectionRepository.findById(id)
                .orElseThrow(() -> new CollectionNotFoundException(id));
    }
}