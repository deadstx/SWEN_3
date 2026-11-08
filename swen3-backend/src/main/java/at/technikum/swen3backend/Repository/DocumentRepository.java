package at.technikum.swen3backend.Repository;

import at.technikum.swen3backend.Entity.DocumentEntity;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends Repository<DocumentEntity, Long> {
    DocumentEntity save(DocumentEntity document);

    List<DocumentEntity> findAll();

    List<DocumentEntity> findAllById(Iterable<Long> ids);

    Optional<DocumentEntity> findById(Long id);

    void delete(DocumentEntity document);
}