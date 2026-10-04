package com.bankapp.frauddetection.rules;

import com.bankapp.frauddetection.dto.TransactionEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;


@Component
public class VelocityRule implements FraudRule {

    private final int maxTransactions;
    private final Duration window;
    private final double score;

    private final Map<UUID, Deque<Instant>> recentTransactionsByAccount = new ConcurrentHashMap<>();

    public VelocityRule(
            @Value("${app.fraud.rules.velocity.max-transactions}") int maxTransactions,
            @Value("${app.fraud.rules.velocity.window-minutes}") long windowMinutes,
            @Value("${app.fraud.rules.velocity.score}") double score) {
        this.maxTransactions = maxTransactions;
        this.window = Duration.ofMinutes(windowMinutes);
        this.score = score;
    }

    @Override
    public RuleResult evaluate(TransactionEvent tx) {
        UUID accountId = tx.getFromAccountId();
        Instant now = tx.getTimestamp() != null ? tx.getTimestamp() : Instant.now();

        Deque<Instant> window = recentTransactionsByAccount.computeIfAbsent(accountId, id -> new ConcurrentLinkedDeque<>());

        synchronized (window) {
            Instant cutoff = now.minus(this.window);
            while (!window.isEmpty() && window.peekFirst().isBefore(cutoff)) {
                window.pollFirst();
            }
            window.addLast(now);
            int count = window.size();

            if (count > maxTransactions) {
                return RuleResult.triggered(score,
                        "Velocity: %d transactions in %d min (limit %d)"
                                .formatted(count, this.window.toMinutes(), maxTransactions));
            }
        }
        return RuleResult.notTriggered();
    }
}
