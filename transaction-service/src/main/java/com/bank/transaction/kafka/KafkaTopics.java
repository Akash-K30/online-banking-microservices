package com.bank.transaction.kafka;

public final class KafkaTopics {
    public static final String ACCOUNT_EVENTS = "account.events";
    public static final String TRANSACTION_EVENTS = "transaction.events";
    public static final String FRAUD_VERDICTS = "fraud.verdicts";

    private KafkaTopics() {
    }
}
