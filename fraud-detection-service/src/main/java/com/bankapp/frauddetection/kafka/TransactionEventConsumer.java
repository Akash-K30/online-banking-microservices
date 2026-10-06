package com.bankapp.frauddetection.kafka;

import com.bankapp.frauddetection.dto.FraudVerdict;
import com.bankapp.frauddetection.dto.TransactionEvent;
import com.bankapp.frauddetection.service.FraudCheckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class TransactionEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventConsumer.class);

    private final FraudCheckService fraudCheckService;
    private final FraudVerdictProducer fraudVerdictProducer;

    public TransactionEventConsumer(FraudCheckService fraudCheckService, FraudVerdictProducer fraudVerdictProducer) {
        this.fraudCheckService = fraudCheckService;
        this.fraudVerdictProducer = fraudVerdictProducer;
    }

    @KafkaListener(topics = "${app.kafka.topics.transaction-events}")
    public void onTransactionEvent(TransactionEvent event) {
        if (event.getType() != TransactionEvent.EventType.TRANSACTION_INITIATED) {
            log.trace("Ignoring {} event for transaction {}", event.getType(), event.getTransactionId());
            return;
        }

        try {
            Optional<FraudVerdict> verdict = fraudCheckService.process(event);
            verdict.ifPresent(fraudVerdictProducer::publish);
        } catch (Exception e) {
            log.error("Failed to process transaction {}", event.getTransactionId(), e);
        }
    }
}
