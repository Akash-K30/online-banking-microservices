package com.bank.userservice.dto;

import com.bank.userservice.entity.KycStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class
KycStatusUpdateRequest {

    @NotNull(message = "kycStatus is required")
    private KycStatus kycStatus;
}
