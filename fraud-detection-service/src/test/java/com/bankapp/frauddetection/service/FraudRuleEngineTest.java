package com.bankapp.frauddetection.service;

import com.bankapp.frauddetection.dto.FraudVerdict;
import com.bankapp.frauddetection.dto.TransactionEvent;
import com.bankapp.frauddetection.rules.FraudRule;
import com.bankapp.frauddetection.rules.RuleResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Because rules are just Strategy implementations, the engine can be tested
 * with trivial fake rules instead of spinning up real Kafka/Postgres-backed
 * ones - that decoupling is the main payoff of the pattern.
 */
class FraudRuleEngineTest {

    private static TransactionEvent sampleTx() {
        return new TransactionEvent(
                TransactionEvent.EventType.TransactionInitiated,
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("500"), "USD", "device-1", "Panipat, IN", Instant.now());
    }

    private static FraudRule alwaysScores(double score, String reason) {
        return tx -> RuleResult.triggered(score, reason);
    }

    private static FraudRule neverTriggers() {
        return tx -> RuleResult.notTriggered();
    }

    @Test
    void allowsWhenNoRulesTrigger() {
        FraudRuleEngine engine = new FraudRuleEngine(List.of(neverTriggers(), neverTriggers()), 70, 35);

        FraudVerdict verdict = engine.evaluate(sampleTx());

        assertThat(verdict.getVerdict()).isEqualTo(FraudVerdict.Verdict.ALLOW);
        assertThat(verdict.getScore()).isZero();
        assertThat(verdict.getReasons()).isEmpty();
    }

    @Test
    void reviewsWhenScoreCrossesReviewButNotBlockThreshold() {
        FraudRuleEngine engine = new FraudRuleEngine(
                List.of(alwaysScores(40, "new device")), 70, 35);

        FraudVerdict verdict = engine.evaluate(sampleTx());

        assertThat(verdict.getVerdict()).isEqualTo(FraudVerdict.Verdict.REVIEW);
        assertThat(verdict.getReasons()).containsExactly("new device");
    }

    @Test
    void blocksWhenCombinedScoreCrossesBlockThreshold() {
        FraudRuleEngine engine = new FraudRuleEngine(
                List.of(alwaysScores(40, "velocity"), alwaysScores(35, "large amount")), 70, 35);

        FraudVerdict verdict = engine.evaluate(sampleTx());

        assertThat(verdict.getVerdict()).isEqualTo(FraudVerdict.Verdict.BLOCK);
        assertThat(verdict.getScore()).isEqualTo(75.0);
        assertThat(verdict.getReasons()).containsExactly("velocity", "large amount");
    }

    @Test
    void blacklistHitAloneIsEnoughToBlock() {
        FraudRuleEngine engine = new FraudRuleEngine(
                List.of(alwaysScores(100, "blacklisted account")), 70, 35);

        FraudVerdict verdict = engine.evaluate(sampleTx());

        assertThat(verdict.getVerdict()).isEqualTo(FraudVerdict.Verdict.BLOCK);
    }
}
