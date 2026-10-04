package com.bank.transaction.exception;

import java.util.UUID;

public class TransactionNotFoundException extends TransferException {
    public TransactionNotFoundException(UUID transactionId) {
        super("No transaction found with id " + transactionId);
    }
}
