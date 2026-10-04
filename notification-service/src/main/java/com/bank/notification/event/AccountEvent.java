package com.bank.notification.event;

import java.math.BigDecimal;
import java.util.UUID;


public class AccountEvent {

    private String type;
    private UUID accountId;
    private UUID userId;
    private BigDecimal amount; // present for Debited/Credited
    private String reason;     // present for Frozen

    public AccountEvent() {
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
