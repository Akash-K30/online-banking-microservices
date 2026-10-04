package com.bank.notification.dlt;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;


@Component
public class DeadLetterProducer {

    private static final String TOPIC = "notification.dlt";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DeadLetterProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(String notificationId, Object failedPayload, String errorMessage) {
        Map<String, Object> dltMessage = Map.of(
                "notificationId", notificationId,
                "payload", failedPayload,
                "error", errorMessage,
                "failedAt", Instant.now().toString()
        );
        kafkaTemplate.send(TOPIC, notificationId, dltMessage);
    }
}
