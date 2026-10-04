package com.bankapp.frauddetection.rules;

import com.bankapp.frauddetection.dto.TransactionEvent;
import com.bankapp.frauddetection.repository.KnownDeviceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class NewDeviceOrLocationRule implements FraudRule {

    private final KnownDeviceRepository knownDeviceRepository;
    private final double score;

    public NewDeviceOrLocationRule(KnownDeviceRepository knownDeviceRepository,
                                    @Value("${app.fraud.rules.new-device-or-location.score}") double score) {
        this.knownDeviceRepository = knownDeviceRepository;
        this.score = score;
    }

    @Override
    public RuleResult evaluate(TransactionEvent tx) {
        if (tx.getFromAccountId() == null || tx.getDeviceId() == null || tx.getLocation() == null) {
            return RuleResult.notTriggered();
        }
        boolean known = knownDeviceRepository.existsByAccountIdAndDeviceIdAndLocation(
                tx.getFromAccountId(), tx.getDeviceId(), tx.getLocation());

        if (!known) {
            return RuleResult.triggered(score,
                    "New device/location for account: %s @ %s".formatted(tx.getDeviceId(), tx.getLocation()));
        }
        return RuleResult.notTriggered();
    }
}
