package com.bank.transaction.dto;

public enum AccountEventType {
    ACCOUNT_OPENED,
    ACCOUNT_DEBITED,
    ACCOUNT_CREDITED,
    ACCOUNT_FROZEN,
    DEBIT,
    CREDIT,
    HOLD,
    RELEASE
}
