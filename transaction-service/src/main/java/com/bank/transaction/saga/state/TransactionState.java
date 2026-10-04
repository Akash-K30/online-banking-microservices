package com.bank.transaction.saga.state;

import com.bank.transaction.dto.FraudVerdictMessage;
import com.bank.transaction.entity.Transaction;
import com.bank.transaction.saga.TransactionSagaOrchestrator;

/**
 * One implementation per {@link com.bank.transaction.entity.TransactionStatus}. Each
 * state decides whether a fraud verdict is expected while the transaction is in it,
 * and what should happen next. Kafka delivery is at-least-once, so an unexpected or
 * duplicate verdict is logged and ignored rather than thrown as an error.
 */
public interface TransactionState {

    void onFraudVerdict(Transaction transaction, FraudVerdictMessage verdict, TransactionSagaOrchestrator orchestrator);
}
