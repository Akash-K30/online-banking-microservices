package com.bankapp.frauddetection.rules;


public record RuleResult(boolean triggered, double score, String reason) {

    public static RuleResult notTriggered() {
        return new RuleResult(false, 0.0, null);
    }

    public static RuleResult triggered(double score, String reason) {
        return new RuleResult(true, score, reason);
    }
}
