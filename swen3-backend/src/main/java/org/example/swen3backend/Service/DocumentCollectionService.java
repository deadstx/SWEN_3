package org.example.swen3backend.service;

import lombok.RequiredArgsConstructor;
import org.example.swen3backend.dto.DocumentCollectionResponse;
import org.example.swen3backend.entity.DocumentCollectionEntity;
import org.example.swen3backend.entity.DocumentEntity;
import org.example.swen3backend.mapper.DocumentCollectionMapper;
import org.example.swen3backend.repository.DocumentCollectionRepository;
import org.example.swen3backend.repository.DocumentRepository;
import org.example.swen3backend.validation.FieldValidator;
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