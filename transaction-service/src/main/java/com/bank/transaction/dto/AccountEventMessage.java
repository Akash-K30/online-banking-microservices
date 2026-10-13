package com.bank.transaction.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


public record AccountEventMessage(
        AccountEventType entryType,
        UUID accountId,
        UUID userId,
        BigDecimal amount,
        String TransactionRef,
        Instant occurredAt
) {
}
