package com.bankapp.frauddetection.service;

import com.bankapp.frauddetection.dto.FraudVerdict;
import com.bankapp.frauddetection.dto.TransactionEvent;
import com.bankapp.frauddetection.entity.FraudCheck;
import com.bankapp.frauddetection.entity.KnownDevice;
import com.bankapp.frauddetection.repository.FraudCheckRepository;
import com.bankapp.frauddetection.repository.KnownDeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;


@Service
public class FraudCheckService {

    private static final Logger log = LoggerFactory.getLogger(FraudCheckService.class);

    private final FraudRuleEngine ruleEngine;
    private final FraudCheckRepository fraudCheckRepository;
    private final KnownDeviceRepository knownDeviceRepository;

    public FraudCheckService(FraudRuleEngine ruleEngine,
                              FraudCheckRepository fraudCheckRepository,
                              KnownDeviceRepository knownDeviceRepository) {
        this.ruleEngine = ruleEngine;
        this.fraudCheckRepository = fraudCheckRepository;
        this.knownDeviceRepository = knownDeviceRepository;
    }

    @Transactional
    public Optional<FraudVerdict> process(TransactionEvent tx) {
        if (fraudCheckRepository.existsByTransactionId(tx.getTransactionId())) {
            log.info("Transaction {} already evaluated, skipping (likely redelivery)", tx.getTransactionId());
            return Optional.empty();
        }

        FraudVerdict verdict = ruleEngine.evaluate(tx);

        fraudCheckRepository.save(FraudCheck.builder()
                .transactionId(tx.getTransactionId())
                .score(verdict.getScore())
                .verdict(verdict.getVerdict().name())
                .reasons(verdict.getReasons())
                .createdAt(verdict.getEvaluatedAt())
                .build());

        rememberDevice(tx);

        log.info("Transaction {} evaluated: {} (score={}, reasons={})",
                tx.getTransactionId(), verdict.getVerdict(), verdict.getScore(), verdict.getReasons());

        return Optional.of(verdict);
    }

    private void rememberDevice(TransactionEvent tx) {
        if (tx.getFromAccountId() == null || tx.getDeviceId() == null || tx.getLocation() == null) {
            return;
        }
        knownDeviceRepository.findByAccountIdAndDeviceIdAndLocation(tx.getFromAccountId(), tx.getDeviceId(), tx.getLocation())
                .ifPresentOrElse(
                        existing -> {
                            existing.setLastSeenAt(Instant.now());
                            knownDeviceRepository.save(existing);
                        },
                        () -> knownDeviceRepository.save(KnownDevice.builder()
                                .accountId(tx.getFromAccountId())
                                .deviceId(tx.getDeviceId())
                                .location(tx.getLocation())
                                .firstSeenAt(Instant.now())
                                .lastSeenAt(Instant.now())
                                .build())
                );
    }
}
