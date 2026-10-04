package com.bank.transaction.service;

import com.bank.transaction.dto.TransferRequest;
import com.bank.transaction.entity.Transaction;
import com.bank.transaction.exception.IdempotencyConflictException;
import com.bank.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class IdempotencyGuard {

    private final TransactionRepository transactionRepository;

    public IdempotencyGuard(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Optional<Transaction> findExisting(TransferRequest request) {
        return transactionRepository.findByIdempotencyKey(request.idempotencyKey())
                .map(existing -> {
                    if (!existing.getFromAccountId().equals(request.fromAccountId())
                            || !existing.getToAccountId().equals(request.toAccountId())
                            || existing.getAmount().compareTo(request.amount()) != 0) {
                        throw new IdempotencyConflictException(
                                "idempotencyKey " + request.idempotencyKey() + " was already used for a different transfer");
                    }
                    return existing;
                });
    }
}
