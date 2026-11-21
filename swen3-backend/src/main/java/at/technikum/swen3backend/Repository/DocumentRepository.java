package at.technikum.swen3backend.Repository;

import at.technikum.swen3backend.Entity.DocumentEntity;
import org.springframework.data.repository.Repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends Repository<DocumentEntity, Long> {
    DocumentEntity save(DocumentEntity document);

    List<DocumentEntity> findAll();

    List<DocumentEntity> findAllById(Iterable<Long> ids);

    Optional<DocumentEntity> findById(Long id);

    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("""
            update DocumentEntity d set d.summary = :summary, d.summaryStatus = :status
            where d.id = :id and d.objectKey = :objectKey and d.summaryStatus = 'PENDING'
            """)
    int finishSummary(@Param("id") long id, @Param("objectKey") String objectKey,
                      @Param("summary") String summary, @Param("status") String status);

    void delete(DocumentEntity document);
}