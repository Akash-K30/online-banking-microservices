package com.bank.transaction.repository;

import com.bank.transaction.entity.Transaction;
import com.bank.transaction.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    List<Transaction> findByFromUserIdOrToUserIdOrderByCreatedAtDesc(UUID fromUserId, UUID toUserId);

    List<Transaction> findByStatusInAndUpdatedAtBefore(List<TransactionStatus> statuses, Instant cutoff);
}
