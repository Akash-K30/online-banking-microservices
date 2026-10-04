package com.bank.transaction.entity;

public enum TransactionStatus {
    INITIATED,
    FRAUD_CHECK,
    HOLD_PLACED,
    DEBITED,
    COMPLETED,
    FAILED
}
