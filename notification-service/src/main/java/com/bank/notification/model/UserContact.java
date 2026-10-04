package com.bank.notification.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "user_contact")
public class UserContact {

    @Id
    private UUID userId;

    private String email;
    private String phone;
    private Instant updatedAt;

    protected UserContact() {
    }

    public UserContact(UUID userId, String email, String phone) {
        this.userId = userId;
        this.email = email;
        this.phone = phone;
        this.updatedAt = Instant.now();
    }

    public void update(String email, String phone) {
        this.email = email;
        this.phone = phone;
        this.updatedAt = Instant.now();
    }

    public UUID getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Instant getUpdatedAt() { return updatedAt; }
}
