package com.bank.transaction.saga.state;

import com.bank.transaction.dto.FraudVerdictMessage;
import com.bank.transaction.entity.Transaction;
import com.bank.transaction.saga.TransactionSagaOrchestrator;
import org.springframework.stereotype.Component;

@Component
public class FraudCheckState implements TransactionState {

    @Override
    public void onFraudVerdict(Transaction transaction, FraudVerdictMessage verdict, TransactionSagaOrchestrator orchestrator) {
        switch (verdict.verdict()) {
            case ALLOW -> orchestrator.runHoldDebitCredit(transaction);
            case BLOCK -> orchestrator.failWithoutCompensation(transaction,
                    "Blocked by fraud detection (score=" + verdict.score() + ")");
            // TODO: REVIEW should route to a human adjudication queue rather than
            case REVIEW -> orchestrator.failWithoutCompensation(transaction,
                    "Flagged for manual review (score=" + verdict.score() + ")");
        }
    }
}
