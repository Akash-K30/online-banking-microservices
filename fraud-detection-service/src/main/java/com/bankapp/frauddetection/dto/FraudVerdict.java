package com.bankapp.frauddetection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class FraudVerdict {

    public enum Verdict {
        ALLOW,
        REVIEW,
        BLOCK
    }

    private UUID transactionId;
    private Verdict verdict;
    private double score;
    private List<String> reasons;
    private Instant evaluatedAt;
}
