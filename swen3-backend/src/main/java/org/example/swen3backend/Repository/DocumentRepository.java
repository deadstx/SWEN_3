package org.example.swen3backend.repository;

import org.example.swen3backend.entity.DocumentEntity;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends Repository<DocumentEntity, Long> {
    DocumentEntity save(DocumentEntity document);

    List<DocumentEntity> findAll();

    Optional<DocumentEntity> findById(Long id);

    void delete(DocumentEntity document);
}