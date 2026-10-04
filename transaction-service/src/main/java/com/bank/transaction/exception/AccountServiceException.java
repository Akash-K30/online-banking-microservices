package com.bank.transaction.exception;

public class AccountServiceException extends TransferException {
    public AccountServiceException(String message) {
        super(message);
    }

    public AccountServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
