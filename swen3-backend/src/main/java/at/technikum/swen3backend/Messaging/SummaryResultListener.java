package at.technikum.swen3backend.Messaging;

import at.technikum.swen3backend.Repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SummaryResultListener {
    private final DocumentRepository documents;

    @RabbitListener(queues = RabbitConfig.SUMMARY_RESULT_QUEUE)
    public void storeResult(SummaryResult result) {
        if (result.documentId() <= 0 || result.objectKey() == null || result.objectKey().isBlank()
                || result.status() == null
                || (result.status() == Status.COMPLETED && (result.summary() == null
                || result.summary().isBlank() || result.summary().length() > 40_000))) {
            throw new AmqpRejectAndDontRequeueException("Invalid summary result");
        }

        String summary = result.status() == Status.COMPLETED ? result.summary().strip() : null;
        int updated = documents.finishSummary(result.documentId(), result.objectKey(), summary, result.status().name());
        if (updated == 0) {
            log.info("Ignored duplicate or obsolete summary result for document id={}", result.documentId());
        } else {
            log.info("Stored summary status {} for document id={}", result.status(), result.documentId());
        }
    }

    public record SummaryResult(long documentId, String objectKey, Status status, String summary) {
    }

    public enum Status {
        COMPLETED, NO_TEXT, FAILED
    }
}
