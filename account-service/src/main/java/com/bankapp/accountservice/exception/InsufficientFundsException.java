package com.bankapp.accountservice.exception;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(UUID accountId, BigDecimal requested, BigDecimal available) {
        super("Account %s has insufficient funds: requested %s, available %s"
                .formatted(accountId, requested, available));
    }
}
