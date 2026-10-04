package com.bankapp.accountservice.exception;

import java.util.UUID;

public class AccountFrozenException extends RuntimeException {
    public AccountFrozenException(UUID accountId) {
        super("Account is not ACTIVE and cannot be moved: " + accountId);
    }
}
