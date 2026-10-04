package com.bank.transaction.kafka;

import com.bank.transaction.dto.FraudVerdictMessage;
import com.bank.transaction.saga.TransactionSagaOrchestrator;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class FraudVerdictListener {

    private final TransactionSagaOrchestrator orchestrator;

    public FraudVerdictListener(TransactionSagaOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @KafkaListener(topics = KafkaTopics.FRAUD_VERDICTS, groupId = "${spring.kafka.consumer.group-id}")
    public void onFraudVerdict(FraudVerdictMessage message) {
        orchestrator.handleFraudVerdict(message);
    }
}
