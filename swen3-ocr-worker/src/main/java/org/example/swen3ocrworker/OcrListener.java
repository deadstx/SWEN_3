package org.example.swen3ocrworker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OcrListener {

    @RabbitListener(queues = RabbitConfig.OCR_QUEUE)
    public void receive(OcrMessage message) {
        log.info("Received OCR job for document id={}, file={}",
                message.documentId(), message.fileName());

        // Sprint 4 hier dann OCR-Verarbeitung usw
    }
}