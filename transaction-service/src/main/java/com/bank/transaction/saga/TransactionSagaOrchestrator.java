package com.bank.transaction.saga;

import com.bank.transaction.client.AccountServiceClient;
import com.bank.transaction.dto.FraudVerdictMessage;
import com.bank.transaction.entity.Transaction;
import com.bank.transaction.entity.TransactionStatus;
import com.bank.transaction.event.TransactionEventMessage;
import com.bank.transaction.exception.AccountServiceException;
import com.bank.transaction.exception.TransactionNotFoundException;
import com.bank.transaction.kafka.TransactionEventProducer;
import com.bank.transaction.repository.TransactionRepository;
import com.bank.transaction.saga.state.TransactionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
public class TransactionSagaOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(TransactionSagaOrchestrator.class);

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;
    private final TransactionEventProducer eventProducer;
    private final Map<TransactionStatus, TransactionState> states;

    public TransactionSagaOrchestrator(TransactionRepository transactionRepository,
                                        AccountServiceClient accountServiceClient,
                                        TransactionEventProducer eventProducer,
                                        Map<TransactionStatus, TransactionState> states) {
        this.transactionRepository = transactionRepository;
        this.accountServiceClient = accountServiceClient;
        this.eventProducer = eventProducer;
        this.states = states;
    }

    public void handleFraudVerdict(FraudVerdictMessage verdict) {
        Transaction transaction = transactionRepository.findById(verdict.transactionId())
                .orElseThrow(() -> new TransactionNotFoundException(verdict.transactionId()));

        TransactionState state = states.get(transaction.getStatus());
        if (state == null) {
            log.error("No TransactionState registered for status {}", transaction.getStatus());
            return;
        }
        state.onFraudVerdict(transaction, verdict, this);
    }

    /** Called by {@code FraudCheckState} when the verdict is ALLOW. */
    public void runHoldDebitCredit(Transaction transaction) {
        String refId = transaction.getId().toString();

        try {
            accountServiceClient.placeHold(transaction.getFromAccountId(), transaction.getAmount(), refId);
        } catch (AccountServiceException e) {
            log.warn("Hold failed for transaction {}: {}", transaction.getId(), e.getMessage());
            failWithoutCompensation(transaction, "Could not place hold: " + e.getMessage());
            return;
        }
        transaction.transitionTo(TransactionStatus.HOLD_PLACED);
        transactionRepository.save(transaction);

        try {
            accountServiceClient.debit(transaction.getFromAccountId(), transaction.getAmount(), refId);
        } catch (AccountServiceException e) {
            log.warn("Debit failed for transaction {}: {}", transaction.getId(), e.getMessage());
            releaseHoldThenFail(transaction, "Could not debit source account: " + e.getMessage());
            return;
        }
        transaction.transitionTo(TransactionStatus.DEBITED);
        transactionRepository.save(transaction);

        try {
            accountServiceClient.credit(transaction.getToAccountId(), transaction.getAmount(), refId);
        } catch (AccountServiceException e) {
            log.error("Credit failed for transaction {} after debit succeeded - compensating: {}",
                    transaction.getId(), e.getMessage());
            compensateDebitThenFail(transaction, "Could not credit destination account: " + e.getMessage());
            return;
        }

        transaction.transitionTo(TransactionStatus.COMPLETED);
        transactionRepository.save(transaction);
        eventProducer.publish(TransactionEventMessage.completed(
                transaction.getId(), transaction.getFromAccountId(), transaction.getToAccountId(), transaction.getAmount()));
    }

    /** Called by {@code FraudCheckState} when the verdict is BLOCK or REVIEW. */
    public void failWithoutCompensation(Transaction transaction, String reason) {
        transaction.fail(reason);
        transactionRepository.save(transaction);
        eventProducer.publish(TransactionEventMessage.failed(
                transaction.getId(), transaction.getFromAccountId(), transaction.getToAccountId(), transaction.getAmount(), reason));
    }

    private void releaseHoldThenFail(Transaction transaction, String reason) {
        String refId = transaction.getId().toString();
        try {
            accountServiceClient.releaseHold(transaction.getFromAccountId(), refId);
        } catch (AccountServiceException releaseFailure) {
            log.error("Failed to release hold for transaction {} - funds may remain reserved, needs manual reconciliation: {}",
                    transaction.getId(), releaseFailure.getMessage());
            reason = reason + " (hold release also failed - manual reconciliation required)";
        }
        failWithoutCompensation(transaction, reason);
    }

    private void compensateDebitThenFail(Transaction transaction, String reason) {
        String refId = transaction.getId().toString();
        try {

            accountServiceClient.credit(transaction.getFromAccountId(), transaction.getAmount(), refId);
        } catch (AccountServiceException compensationFailure) {
            log.error("Compensating credit back to {} failed for transaction {} - manual reconciliation required: {}",
                    transaction.getFromAccountId(), transaction.getId(), compensationFailure.getMessage());
            reason = reason + " (compensating reversal also failed - manual reconciliation required)";
        }
        failWithoutCompensation(transaction, reason);
    }
}
