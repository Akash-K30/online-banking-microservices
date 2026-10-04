package com.bank.transaction.controller;

import com.bank.transaction.dto.TransactionResponse;
import com.bank.transaction.dto.TransferRequest;
import com.bank.transaction.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
        TransactionResponse response = transferService.initiateTransfer(request);
        // 202: the transfer is accepted but completes asynchronously once the fraud
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/transactions/{id}")
    public TransactionResponse getTransaction(@PathVariable UUID id) {
        return transferService.getById(id);
    }

    @GetMapping("/transactions")
    public List<TransactionResponse> getTransactionsByUser(@RequestParam UUID userId) {
        return transferService.getByUserId(userId);
    }
}
