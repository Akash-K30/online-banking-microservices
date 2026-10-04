package com.bankapp.accountservice.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OpenAccountRequest(
        @NotNull UUID userId
) {
}
