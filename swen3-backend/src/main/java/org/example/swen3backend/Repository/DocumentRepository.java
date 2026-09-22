package org.example.swen3backend.repository;

import org.example.swen3backend.entity.DocumentEntity;
import org.springframework.data.repository.Repository;

public interface DocumentRepository extends Repository<DocumentEntity, Long> {
    DocumentEntity save(DocumentEntity document);
}