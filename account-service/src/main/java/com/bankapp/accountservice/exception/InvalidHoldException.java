package com.bankapp.accountservice.exception;

import java.math.BigDecimal;
import java.util.UUID;

public class InvalidHoldException extends RuntimeException {

    private InvalidHoldException(String message) {
        super(message);
    }

    public static InvalidHoldException noMatchingHold(UUID accountId, String refId) {
        return new InvalidHoldException(
                "No hold found for account %s with refId %s - debit must be preceded by a hold on the same refId"
                        .formatted(accountId, refId));
    }

    public static InvalidHoldException holdAlreadyReleased(UUID accountId, String refId) {
        return new InvalidHoldException(
                "Hold for account %s with refId %s was already released and cannot be debited"
                        .formatted(accountId, refId));
    }

    public static InvalidHoldException amountMismatch(UUID accountId, String refId,
                                                        BigDecimal holdAmount, BigDecimal debitAmount) {
        return new InvalidHoldException(
                "Debit amount %s for account %s / refId %s does not match held amount %s"
                        .formatted(debitAmount, accountId, refId, holdAmount));
    }
}
