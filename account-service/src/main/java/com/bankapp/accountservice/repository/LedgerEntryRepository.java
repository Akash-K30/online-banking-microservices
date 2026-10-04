package com.bankapp.accountservice.repository;

import com.bankapp.accountservice.model.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    List<LedgerEntry> findByAccountIdOrderByCreatedAtAsc(UUID accountId);

    /** Used to detect a retried request (e.g. Transaction Service retrying a debit call). */
    List<LedgerEntry> findByAccountIdAndTransactionRef(UUID accountId, String transactionRef);
}
