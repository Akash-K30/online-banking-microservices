package com.bank.transaction.dto;

import java.util.List;
import java.util.UUID;

public record FraudVerdictMessage(
        UUID transactionId,
        FraudVerdictType verdict,
        double score,
        List<String> reasons
) {
}
