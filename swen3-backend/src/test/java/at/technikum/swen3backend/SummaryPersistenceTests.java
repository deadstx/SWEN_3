package at.technikum.swen3backend;

import at.technikum.swen3backend.Entity.DocumentEntity;
import at.technikum.swen3backend.Mapper.DocumentMapper;
import at.technikum.swen3backend.Messaging.RabbitConfig;
import at.technikum.swen3backend.Messaging.SummaryResultListener;
import at.technikum.swen3backend.Messaging.SummaryResultListener.Status;
import at.technikum.swen3backend.Messaging.SummaryResultListener.SummaryResult;
import at.technikum.swen3backend.Repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(properties = "spring.sql.init.mode=never")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class SummaryPersistenceTests {
    @Autowired
    private DocumentRepository documents;

    @Autowired
    private PlatformTransactionManager transactions;

    @Test
    void persistsResultForTheMatchingUploadOnce() {
        var document = documents.save(new DocumentEntity("test.pdf", "documents/test.pdf", 10, Instant.now()));
        var listener = new SummaryResultListener(documents);
        long id = document.getId();
        String key = document.getObjectKey();

        listener.storeResult(new SummaryResult(id, "documents/old.pdf", Status.COMPLETED, "Old result"));
        assertEquals("PENDING", documents.findById(id).orElseThrow().getSummaryStatus());

        var properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setInferredArgumentType(SummaryResult.class);
        var message = new Message("""
                {"documentId": %d, "objectKey": "%s", "status": "COMPLETED", "summary": "Persisted summary"}
                """.formatted(id, key).getBytes(StandardCharsets.UTF_8), properties);
        var result = new RabbitConfig().jsonMessageConverter().fromMessage(message);
        listener.storeResult((SummaryResult) result);
        listener.storeResult(new SummaryResult(id, key, Status.FAILED, null));

        var response = new TransactionTemplate(transactions).execute(transaction ->
                new DocumentMapper().toDto(documents.findById(id).orElseThrow()));
        assertEquals("COMPLETED", response.summaryStatus());
        assertEquals("Persisted summary", response.summary());
    }
}
