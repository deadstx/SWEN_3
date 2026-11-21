package at.technikum.swen3backend.Messaging;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String OCR_QUEUE = "ocr.queue";
    public static final String SUMMARY_RESULT_QUEUE = "genai.result.queue";

    @Bean
    public Queue ocrQueue() {
        return new Queue(OCR_QUEUE, true);
    }

    @Bean
    public Queue summaryResultQueue() {
        return QueueBuilder.durable(SUMMARY_RESULT_QUEUE)
                .deadLetterExchange("")
                .deadLetterRoutingKey(SUMMARY_RESULT_QUEUE + ".dead")
                .build();
    }

    @Bean
    public Queue summaryResultDeadLetterQueue() {
        return new Queue(SUMMARY_RESULT_QUEUE + ".dead", true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}