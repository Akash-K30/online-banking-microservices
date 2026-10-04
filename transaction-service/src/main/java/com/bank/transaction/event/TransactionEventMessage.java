package com.bank.transaction.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


public record TransactionEventMessage(
        TransactionEventType type,
        UUID transactionId,
        UUID fromAccountId,
        UUID toAccountId,
        BigDecimal amount,
        String failureReason,
        Instant occurredAt
) {
    public static TransactionEventMessage initiated(UUID transactionId, UUID fromAccountId, UUID toAccountId, BigDecimal amount) {
        return new TransactionEventMessage(TransactionEventType.TRANSACTION_INITIATED, transactionId, fromAccountId, toAccountId, amount, null, Instant.now());
    }

    public static TransactionEventMessage completed(UUID transactionId, UUID fromAccountId, UUID toAccountId, BigDecimal amount) {
        return new TransactionEventMessage(TransactionEventType.TRANSACTION_COMPLETED, transactionId, fromAccountId, toAccountId, amount, null, Instant.now());
    }

    public static TransactionEventMessage failed(UUID transactionId, UUID fromAccountId, UUID toAccountId, BigDecimal amount, String reason) {
        return new TransactionEventMessage(TransactionEventType.TRANSACTION_FAILED, transactionId, fromAccountId, toAccountId, amount, reason, Instant.now());
    }
}
