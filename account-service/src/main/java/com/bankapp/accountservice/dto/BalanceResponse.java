package com.bankapp.accountservice.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record BalanceResponse(
        UUID accountId,
        BigDecimal balance,
        BigDecimal heldAmount,
        BigDecimal availableBalance,
        String status
) {
}
