package com.bank.transaction.kafka;

import com.bank.transaction.event.TransactionEventMessage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransactionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TransactionEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(TransactionEventMessage event) {

        kafkaTemplate.send(KafkaTopics.TRANSACTION_EVENTS, event.transactionId().toString(), event);
    }
}
