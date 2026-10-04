package com.bankapp.accountservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<?> handleNotFound(AccountNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(ex));
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<?> handleInsufficientFunds(InsufficientFundsException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body(ex));
    }

    @ExceptionHandler(AccountFrozenException.class)
    public ResponseEntity<?> handleFrozen(AccountFrozenException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(ex));
    }

    @ExceptionHandler(InvalidHoldException.class)
    public ResponseEntity<?> handleInvalidHold(InvalidHoldException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(ex));
    }

    private Map<String, Object> body(Exception ex) {
        return Map.of(
                "timestamp", Instant.now().toString(),
                "message", ex.getMessage()
        );
    }
}
