package com.bank.transaction.exception;

public class IdempotencyConflictException extends TransferException {
    public IdempotencyConflictException(String message) {
        super(message);
    }
}
