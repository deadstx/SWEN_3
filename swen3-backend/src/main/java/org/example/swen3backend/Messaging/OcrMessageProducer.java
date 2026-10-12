package org.example.swen3backend.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.swen3backend.service.MessageQueueException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendOcrRequest(OcrMessage message) {
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.OCR_QUEUE, message);
            log.info("OCR message sent for document id={}", message.documentId());
        } catch (AmqpException e) {
            throw new MessageQueueException(
                    "Could not send OCR message for document " + message.documentId(), e);
        }
    }
}