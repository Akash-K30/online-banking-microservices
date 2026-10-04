package com.bankapp.frauddetection.repository;

import com.bankapp.frauddetection.entity.FraudCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FraudCheckRepository extends JpaRepository<FraudCheck, UUID> {

    boolean existsByTransactionId(UUID transactionId);
}
