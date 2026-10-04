package com.bankapp.accountservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Shared shape for hold / release / debit / credit request bodies.
 * refId is the caller's idempotency key (e.g. Transaction Service's
 * transactionId) - the service uses it to make retried calls a no-op.
 */
public record AmountRequest(
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
        @NotBlank String refId
) {
}
