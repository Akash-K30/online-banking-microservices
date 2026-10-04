package com.bank.userservice.event;

import com.bank.userservice.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic.user-events}")
    private String userEventsTopic;

    public void publishUserRegistered(User user) {
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .eventType("UserRegistered")
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .occurredAt(Instant.now())
                .build();

        send(user.getId().toString(), event, "UserRegistered");
    }

    public void publishKycStatusUpdated(User user) {
        KycStatusUpdatedEvent event = KycStatusUpdatedEvent.builder()
                .eventType("KycStatusUpdated")
                .userId(user.getId())
                .kycStatus(user.getKycStatus())
                .occurredAt(Instant.now())
                .build();

        send(user.getId().toString(), event, "KycStatusUpdated");
    }

    /**
     * Key by userId so all events for the same user land on the same partition
     * and are processed in order (mirrors the accountId/transactionId keying
     * used on account.events and transaction.events).
     */
    private void send(String key, Object event, String eventType) {
        kafkaTemplate.send(userEventsTopic, key, event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish {} for key {}: {}", eventType, key, ex.getMessage(), ex);
            } else {
                log.info(
                        "Published {} to {} partition {} offset {}",
                        eventType,
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
