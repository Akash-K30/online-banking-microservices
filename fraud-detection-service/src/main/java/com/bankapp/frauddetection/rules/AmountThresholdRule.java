package com.bankapp.frauddetection.rules;

import com.bankapp.frauddetection.dto.TransactionEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Flags any single transaction at or above a configured amount. */
@Component
public class AmountThresholdRule implements FraudRule {

    private final BigDecimal threshold;
    private final double score;

    public AmountThresholdRule(
            @Value("${app.fraud.rules.amount-threshold.threshold}") BigDecimal threshold,
            @Value("${app.fraud.rules.amount-threshold.score}") double score) {
        this.threshold = threshold;
        this.score = score;
    }

    @Override
    public RuleResult evaluate(TransactionEvent tx) {
        if (tx.getAmount() != null && tx.getAmount().compareTo(threshold) >= 0) {
            return RuleResult.triggered(score,
                    "Amount %s exceeds threshold %s".formatted(tx.getAmount(), threshold));
        }
        return RuleResult.notTriggered();
    }
}
