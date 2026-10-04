package com.bank.userservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Payload published to the {@code user.events} Kafka topic when a user completes
 * registration. Keyed by {@code userId} so all events for one user stay in order
 * on the same partition.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {

    private String eventType;
    private UUID userId;
    private String fullName;
    private String email;
    private Instant occurredAt;
}
