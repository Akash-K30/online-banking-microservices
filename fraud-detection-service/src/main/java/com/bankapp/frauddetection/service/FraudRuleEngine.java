package com.bankapp.frauddetection.service;

import com.bankapp.frauddetection.dto.FraudVerdict;
import com.bankapp.frauddetection.dto.TransactionEvent;
import com.bankapp.frauddetection.rules.FraudRule;
import com.bankapp.frauddetection.rules.RuleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@Component
public class FraudRuleEngine {

    private static final Logger log = LoggerFactory.getLogger(FraudRuleEngine.class);

    private final List<FraudRule> rules;
    private final double blockThreshold;
    private final double reviewThreshold;

    public FraudRuleEngine(List<FraudRule> rules,
                            @Value("${app.fraud.thresholds.block}") double blockThreshold,
                            @Value("${app.fraud.thresholds.review}") double reviewThreshold) {
        this.rules = rules;
        this.blockThreshold = blockThreshold;
        this.reviewThreshold = reviewThreshold;
        log.info("Fraud rule engine initialised with {} rules: {}", rules.size(), rules.stream().map(FraudRule::ruleName).toList());
    }

    public FraudVerdict evaluate(TransactionEvent tx) {
        double totalScore = 0.0;
        List<String> reasons = new ArrayList<>();

        for (FraudRule rule : rules) {
            RuleResult result = rule.evaluate(tx);
            if (result.triggered()) {
                totalScore += result.score();
                reasons.add(result.reason());
                log.debug("Rule {} triggered on tx {}: {}", rule.ruleName(), tx.getTransactionId(), result.reason());
            }
        }

        FraudVerdict.Verdict verdict;
        if (totalScore >= blockThreshold) {
            verdict = FraudVerdict.Verdict.BLOCK;
        } else if (totalScore >= reviewThreshold) {
            verdict = FraudVerdict.Verdict.REVIEW;
        } else {
            verdict = FraudVerdict.Verdict.ALLOW;
        }

        return new FraudVerdict(tx.getTransactionId(), verdict, totalScore, reasons, Instant.now());
    }
}
