package com.bank.transaction.saga.state;

import com.bank.transaction.dto.FraudVerdictMessage;
import com.bank.transaction.entity.Transaction;
import com.bank.transaction.saga.TransactionSagaOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HoldPlacedState implements TransactionState {

    private static final Logger log = LoggerFactory.getLogger(HoldPlacedState.class);

    @Override
    public void onFraudVerdict(Transaction transaction, FraudVerdictMessage verdict, TransactionSagaOrchestrator orchestrator) {
        log.debug("Ignoring duplicate fraud verdict for transaction {} already in HOLD_PLACED", transaction.getId());
    }
}
