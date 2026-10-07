package com.bankapp.frauddetection.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "fraud_checks", indexes = @Index(name = "idx_fraud_checks_transaction_id", columnList = "transaction_id"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "transaction_id", nullable = false, unique = true)
    private UUID transactionId;

    @Column(name = "score", nullable = false)
    private Double score;

    @Column(name = "verdict", nullable = false, length = 16)
    private String verdict;

    @Convert(disableConversion = true)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "reasons", columnDefinition = "jsonb")
    private List<String> reasons;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
