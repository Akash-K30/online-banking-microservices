package com.bankapp.accountservice.service;

import com.bankapp.accountservice.dto.BalanceResponse;
import com.bankapp.accountservice.dto.OpenAccountRequest;
import com.bankapp.accountservice.exception.AccountFrozenException;
import com.bankapp.accountservice.exception.AccountNotFoundException;
import com.bankapp.accountservice.exception.InsufficientFundsException;
import com.bankapp.accountservice.exception.InvalidHoldException;
import com.bankapp.accountservice.model.LedgerEntry;
import com.bankapp.accountservice.model.Account;
import com.bankapp.accountservice.model.AccountStatus;
import com.bankapp.accountservice.model.EntryType;
import com.bankapp.accountservice.repository.AccountRepository;
import com.bankapp.accountservice.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final LedgerService ledgerService;

    @Transactional
    public Account openAccount(OpenAccountRequest request) {
        Account account = Account.builder()
                .id(UUID.randomUUID())
                .userId(request.userId())
                .balance(BigDecimal.ZERO)
                .heldAmount(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        return BalanceResponse.builder()
                .accountId(account.getId())
                .balance(account.getBalance())
                .heldAmount(account.getHeldAmount())
                .availableBalance(account.availableBalance())
                .status(account.getStatus().name())
                .build();
    }


    @Transactional
    public BalanceResponse hold(UUID accountId, BigDecimal amount, String refId) {
        Account account = lockActiveAccount(accountId);
        List<LedgerEntry> existing = ledgerEntryRepository.findByAccountIdAndTransactionRef(accountId, refId);
        if (findByType(existing, EntryType.HOLD).isPresent()) {
            return toBalanceResponse(account); // retry of an already-placed hold
        }
        if (account.availableBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(accountId, amount, account.availableBalance());
        }
        account.setHeldAmount(account.getHeldAmount().add(amount));
        accountRepository.save(account);
        ledgerService.record(accountId, EntryType.HOLD, amount, refId, "AccountHoldPlaced");
        return toBalanceResponse(account);
    }

    /** Releases a previously placed hold without moving funds. */
    @Transactional
    public BalanceResponse release(UUID accountId, BigDecimal amount, String refId) {
        Account account = lockActiveAccount(accountId);
        List<LedgerEntry> existing = ledgerEntryRepository.findByAccountIdAndTransactionRef(accountId, refId);
        if (findByType(existing, EntryType.RELEASE).isPresent()) {
            return toBalanceResponse(account); // retry of an already-applied release
        }
        BigDecimal newHeld = account.getHeldAmount().subtract(amount);
        account.setHeldAmount(newHeld.max(BigDecimal.ZERO));
        accountRepository.save(account);
        ledgerService.record(accountId, EntryType.RELEASE, amount, refId, "AccountHoldReleased");
        return toBalanceResponse(account);
    }


    @Transactional
    public BalanceResponse debit(UUID accountId, BigDecimal amount, String refId) {
        Account account = lockActiveAccount(accountId);
        List<LedgerEntry> existing = ledgerEntryRepository.findByAccountIdAndTransactionRef(accountId, refId);

        if (findByType(existing, EntryType.DEBIT).isPresent()) {
            return toBalanceResponse(account); // retry of an already-applied debit
        }

        LedgerEntry hold = findByType(existing, EntryType.HOLD)
                .orElseThrow(() -> InvalidHoldException.noMatchingHold(accountId, refId));

        if (findByType(existing, EntryType.RELEASE).isPresent()) {
            throw InvalidHoldException.holdAlreadyReleased(accountId, refId);
        }

        if (hold.getAmount().compareTo(amount) != 0) {
            throw InvalidHoldException.amountMismatch(accountId, refId, hold.getAmount(), amount);
        }

        account.setBalance(account.getBalance().subtract(amount));
        account.setHeldAmount(account.getHeldAmount().subtract(amount).max(BigDecimal.ZERO));
        accountRepository.save(account);
        ledgerService.record(accountId, EntryType.DEBIT, amount, refId, "AccountDebited");
        return toBalanceResponse(account);
    }

    /** Moves funds into the account. */
    @Transactional
    public BalanceResponse credit(UUID accountId, BigDecimal amount, String refId) {
        Account account = lockActiveAccount(accountId);
        List<LedgerEntry> existing = ledgerEntryRepository.findByAccountIdAndTransactionRef(accountId, refId);
        if (findByType(existing, EntryType.CREDIT).isPresent()) {
            return toBalanceResponse(account); // retry of an already-applied credit
        }
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        ledgerService.record(accountId, EntryType.CREDIT, amount, refId, "AccountCredited");
        return toBalanceResponse(account);
    }

    private Account lockActiveAccount(UUID accountId) {
        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountFrozenException(accountId);
        }
        return account;
    }


    private Optional<LedgerEntry> findByType(List<LedgerEntry> entries, EntryType type) {
        return entries.stream().filter(e -> e.getEntryType() == type).findFirst();
    }

    private BalanceResponse toBalanceResponse(Account account) {
        return BalanceResponse.builder()
                .accountId(account.getId())
                .balance(account.getBalance())
                .heldAmount(account.getHeldAmount())
                .availableBalance(account.availableBalance())
                .status(account.getStatus().name())
                .build();
    }
}
