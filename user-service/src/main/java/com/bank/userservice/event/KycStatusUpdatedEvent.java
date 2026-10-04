package com.bank.userservice.event;

import com.bank.userservice.entity.KycStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Payload published to the {@code user.events} Kafka topic when a user's KYC
 * status changes (e.g. after a manual review or a call to the KYC provider).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycStatusUpdatedEvent {

    private String eventType;
    private UUID userId;
    private KycStatus kycStatus;
    private Instant occurredAt;
}
