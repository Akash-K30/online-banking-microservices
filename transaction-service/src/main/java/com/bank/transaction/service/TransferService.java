package com.bank.transaction.service;

import com.bank.transaction.dto.TransactionResponse;
import com.bank.transaction.dto.TransferRequest;
import com.bank.transaction.entity.AccountOwnerCache;
import com.bank.transaction.entity.Transaction;
import com.bank.transaction.entity.TransactionStatus;
import com.bank.transaction.event.TransactionEventMessage;
import com.bank.transaction.exception.InvalidTransferException;
import com.bank.transaction.exception.TransactionNotFoundException;
import com.bank.transaction.kafka.TransactionEventProducer;
import com.bank.transaction.repository.AccountOwnerCacheRepository;
import com.bank.transaction.repository.TransactionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TransferService {

    private final TransactionRepository transactionRepository;
    private final AccountOwnerCacheRepository ownerCacheRepository;
    private final IdempotencyGuard idempotencyGuard;
    private final TransactionEventProducer eventProducer;

    public TransferService(TransactionRepository transactionRepository,
                            AccountOwnerCacheRepository ownerCacheRepository,
                            IdempotencyGuard idempotencyGuard,
                            TransactionEventProducer eventProducer) {
        this.transactionRepository = transactionRepository;
        this.ownerCacheRepository = ownerCacheRepository;
        this.idempotencyGuard = idempotencyGuard;
        this.eventProducer = eventProducer;
    }

    @Transactional
    public TransactionResponse initiateTransfer(TransferRequest request) {
        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new InvalidTransferException("fromAccountId and toAccountId must differ");
        }

        Transaction existing = idempotencyGuard.findExisting(request).orElse(null);
        if (existing != null) {
            return TransactionResponse.from(existing);
        }

        Transaction transaction = Transaction.newTransfer(
                request.fromAccountId(), request.toAccountId(), request.amount(), request.idempotencyKey());
        resolveOwners(transaction);

        try {
            transactionRepository.saveAndFlush(transaction);
        } catch (DataIntegrityViolationException concurrentInsert) {
            // Another request with the same idempotencyKey won the race between our
            return transactionRepository.findByIdempotencyKey(request.idempotencyKey())
                    .map(TransactionResponse::from)
                    .orElseThrow(() -> concurrentInsert);
        }

        eventProducer.publish(TransactionEventMessage.initiated(
                transaction.getId(), transaction.getFromAccountId(), transaction.getToAccountId(), transaction.getAmount()));

        transaction.transitionTo(TransactionStatus.FRAUD_CHECK);
        transactionRepository.save(transaction);

        return TransactionResponse.from(transaction);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getById(UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
        return TransactionResponse.from(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getByUserId(UUID userId) {
        return transactionRepository.findByFromUserIdOrToUserIdOrderByCreatedAtDesc(userId, userId).stream()
                .map(TransactionResponse::from)
                .toList();
    }

    private void resolveOwners(Transaction transaction) {
        ownerCacheRepository.findById(transaction.getFromAccountId())
                .map(AccountOwnerCache::getUserId)
                .ifPresent(transaction::setFromUserId);
        ownerCacheRepository.findById(transaction.getToAccountId())
                .map(AccountOwnerCache::getUserId)
                .ifPresent(transaction::setToUserId);
    }
}
