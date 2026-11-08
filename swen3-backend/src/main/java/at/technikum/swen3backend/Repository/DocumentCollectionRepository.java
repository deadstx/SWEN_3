package at.technikum.swen3backend.Repository;

import at.technikum.swen3backend.Entity.DocumentCollectionEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentCollectionRepository extends Repository<DocumentCollectionEntity, Long> {
    DocumentCollectionEntity save(DocumentCollectionEntity collection);
    List<DocumentCollectionEntity> findAll();
    Optional<DocumentCollectionEntity> findById(Long id);
    void delete(DocumentCollectionEntity collection);

    @Modifying
    @Query(value = "delete from collection_documents where document_id = :documentId", nativeQuery = true)
    void removeDocumentReferences(@Param("documentId") long documentId);
}
