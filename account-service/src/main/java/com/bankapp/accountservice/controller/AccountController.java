package com.bankapp.accountservice.controller;

import com.bankapp.accountservice.dto.AmountRequest;
import com.bankapp.accountservice.dto.BalanceResponse;
import com.bankapp.accountservice.dto.OpenAccountRequest;
import com.bankapp.accountservice.model.Account;
import com.bankapp.accountservice.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<Account> openAccount(@Valid @RequestBody OpenAccountRequest request) {
        Account account = accountService.openAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(accountService.getBalance(id));
    }

    @PostMapping("/{id}/hold")
    public ResponseEntity<BalanceResponse> hold(@PathVariable("id") UUID id,
                                                 @Valid @RequestBody AmountRequest request) {
        return ResponseEntity.ok(accountService.hold(id, request.amount(), request.refId()));
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<BalanceResponse> release(@PathVariable("id") UUID id,
                                                    @Valid @RequestBody AmountRequest request) {
        return ResponseEntity.ok(accountService.release(id, request.amount(), request.refId()));
    }

    @PostMapping("/{id}/debit")
    public ResponseEntity<BalanceResponse> debit(@PathVariable("id") UUID id,
                                                  @Valid @RequestBody AmountRequest request) {
        return ResponseEntity.ok(accountService.debit(id, request.amount(), request.refId()));
    }

    @PostMapping("/{id}/credit")
    public ResponseEntity<BalanceResponse> credit(@PathVariable("id") UUID id,
                                                   @Valid @RequestBody AmountRequest request) {
        return ResponseEntity.ok(accountService.credit(id, request.amount(), request.refId()));
    }
}
