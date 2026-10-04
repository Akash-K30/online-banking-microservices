package com.bankapp.accountservice.service;

import com.bankapp.accountservice.model.EntryType;
import com.bankapp.accountservice.model.LedgerEntry;
import com.bankapp.accountservice.model.OutboxEvent;
import com.bankapp.accountservice.model.OutboxStatus;
import com.bankapp.accountservice.repository.LedgerEntryRepository;
import com.bankapp.accountservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;


    @SneakyThrows
    public void record(UUID accountId, EntryType entryType, BigDecimal amount,
                        String transactionRef, String accountEventType) {
        Instant now = Instant.now();

        ledgerEntryRepository.save(LedgerEntry.builder()
                .id(UUID.randomUUID())
                .accountId(accountId)
                .transactionRef(transactionRef)
                .entryType(entryType)
                .amount(amount)
                .createdAt(now)
                .build());

        String payload = objectMapper.writeValueAsString(Map.of(
                "accountId", accountId,
                "entryType", entryType.name(),
                "amount", amount,
                "transactionRef", transactionRef == null ? "" : transactionRef
        ));

        outboxEventRepository.save(OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateId(accountId)
                .eventType(accountEventType)
                .payload(payload)
                .status(OutboxStatus.PENDING)
                .createdAt(now)
                .build());
    }
}
