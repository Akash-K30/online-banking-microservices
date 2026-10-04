package com.bankapp.frauddetection.rules;

import com.bankapp.frauddetection.dto.TransactionEvent;


public interface FraudRule {

    RuleResult evaluate(TransactionEvent tx);

    /** Human-readable name used in verdict reasons and logs. */
    default String ruleName() {
        return getClass().getSimpleName();
    }
}
