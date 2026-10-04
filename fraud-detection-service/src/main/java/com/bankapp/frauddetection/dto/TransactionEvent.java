package com.bankapp.frauddetection.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEvent {

    public enum EventType {
        TRANSACTION_INITIATED,
        TRANSACTION_COMPLETED,
        TRANSACTION_FAILED
    }

    private EventType eventType;
    private UUID transactionId;
    private UUID fromAccountId;
    private UUID toAccountId;
    private BigDecimal amount;
    private String currency;
    private String deviceId;
    private String location;
    private Instant timestamp;
}
