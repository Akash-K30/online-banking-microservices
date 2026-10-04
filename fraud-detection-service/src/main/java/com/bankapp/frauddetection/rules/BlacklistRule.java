package com.bankapp.frauddetection.rules;

import com.bankapp.frauddetection.dto.TransactionEvent;
import com.bankapp.frauddetection.entity.BlacklistEntry;
import com.bankapp.frauddetection.repository.BlacklistRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Flags a transaction whose account, device, or location is on the blacklist. */
@Component
public class BlacklistRule implements FraudRule {

    private final BlacklistRepository blacklistRepository;
    private final double score;

    public BlacklistRule(BlacklistRepository blacklistRepository,
                          @Value("${app.fraud.rules.blacklist.score}") double score) {
        this.blacklistRepository = blacklistRepository;
        this.score = score;
    }

    @Override
    public RuleResult evaluate(TransactionEvent tx) {
        if (tx.getFromAccountId() != null
                && blacklistRepository.existsByTypeAndValue(BlacklistEntry.Type.ACCOUNT_ID, tx.getFromAccountId().toString())) {
            return RuleResult.triggered(score, "Sending account is blacklisted");
        }
        if (tx.getDeviceId() != null
                && blacklistRepository.existsByTypeAndValue(BlacklistEntry.Type.DEVICE_ID, tx.getDeviceId())) {
            return RuleResult.triggered(score, "Device is blacklisted");
        }
        if (tx.getLocation() != null
                && blacklistRepository.existsByTypeAndValue(BlacklistEntry.Type.LOCATION, tx.getLocation())) {
            return RuleResult.triggered(score, "Location is blacklisted");
        }
        return RuleResult.notTriggered();
    }
}
