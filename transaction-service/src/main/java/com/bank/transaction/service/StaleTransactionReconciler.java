package com.bank.transaction.service;

import com.bank.transaction.entity.Transaction;
import com.bank.transaction.entity.TransactionStatus;
import com.bank.transaction.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class StaleTransactionReconciler {

    private static final Logger log = LoggerFactory.getLogger(StaleTransactionReconciler.class);

    private final TransactionRepository transactionRepository;
    private final long staleThresholdSeconds;

    public StaleTransactionReconciler(TransactionRepository transactionRepository,
                                       @Value("${saga.stale-threshold-seconds:60}") long staleThresholdSeconds) {
        this.transactionRepository = transactionRepository;
        this.staleThresholdSeconds = staleThresholdSeconds;
    }

    @Scheduled(fixedDelayString = "${saga.reconciliation-interval-ms:30000}")
    public void reconcile() {
        Instant cutoff = Instant.now().minus(staleThresholdSeconds, ChronoUnit.SECONDS);

        List<Transaction> stuckBeforeMoney = transactionRepository.findByStatusInAndUpdatedAtBefore(
                List.of(TransactionStatus.INITIATED, TransactionStatus.FRAUD_CHECK), cutoff);
        for (Transaction transaction : stuckBeforeMoney) {
            log.warn("Transaction {} stuck in {} for over {}s with no fraud verdict - failing",
                    transaction.getId(), transaction.getStatus(), staleThresholdSeconds);
            transaction.fail("No fraud verdict received within timeout");
            transactionRepository.save(transaction);
        }

        List<Transaction> stuckWithMoneyMoved = transactionRepository.findByStatusInAndUpdatedAtBefore(
                List.of(TransactionStatus.HOLD_PLACED, TransactionStatus.DEBITED), cutoff);
        for (Transaction transaction : stuckWithMoneyMoved) {
            log.error("Transaction {} stuck in {} for over {}s with funds already reserved/moved - needs manual reconciliation",
                    transaction.getId(), transaction.getStatus(), staleThresholdSeconds);
        }
    }
}
