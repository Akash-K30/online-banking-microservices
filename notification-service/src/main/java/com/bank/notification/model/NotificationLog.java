package com.bank.notification.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "notification_log")
public class NotificationLog {

    @Id
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    private String channel;   // ChannelType as string, e.g. "SMS"
    private String template;  // e.g. "TransactionCompletedTemplate"
    private String status;    // "SENT" or "FAILED"

    @Column(name = "created_at")
    private Instant createdAt;

    protected NotificationLog() {
        // required by JPA
    }

    public static NotificationLog of(Notification notification, NotificationStatus status) {
        NotificationLog log = new NotificationLog();
        log.id = UUID.randomUUID();
        log.userId = notification.getUserId();
        log.channel = notification.getChannelType().name();
        log.template = notification.getTemplateName();
        log.status = status.name();
        log.createdAt = Instant.now();
        return log;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getChannel() { return channel; }
    public String getTemplate() { return template; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
