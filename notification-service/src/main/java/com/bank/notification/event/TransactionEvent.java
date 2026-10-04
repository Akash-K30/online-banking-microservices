package com.bank.notification.event;

import java.math.BigDecimal;
import java.util.UUID;


public class TransactionEvent {

    private String type;
    private UUID transactionId;
    private UUID fromUserId;
    private UUID toUserId;
    private BigDecimal amount;
    private String reason; // e.g. "FRAUD", "INSUFFICIENT_FUNDS" — present on TransactionFailed

    public TransactionEvent() {
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public UUID getTransactionId() { return transactionId; }
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }

    public UUID getFromUserId() { return fromUserId; }
    public void setFromUserId(UUID fromUserId) { this.fromUserId = fromUserId; }

    public UUID getToUserId() { return toUserId; }
    public void setToUserId(UUID toUserId) { this.toUserId = toUserId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
