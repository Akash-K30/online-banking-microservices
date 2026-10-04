package com.bank.transaction.exception;

public class InvalidTransferException extends TransferException {
    public InvalidTransferException(String message) {
        super(message);
    }
}
