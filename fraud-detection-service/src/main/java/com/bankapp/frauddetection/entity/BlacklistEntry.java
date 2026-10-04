package com.bankapp.frauddetection.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "blacklist_entries",
        uniqueConstraints = @UniqueConstraint(columnNames = {"type", "value"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistEntry {

    public enum Type {
        ACCOUNT_ID,
        DEVICE_ID,
        LOCATION
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Type type;

    @Column(nullable = false)
    private String value;

    private String reason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
