package com.bank.notification.model;

import java.util.UUID;

public class Notification {

    private final UUID userId;
    private final ChannelType channelType;
    private final String recipient;   // phone number for SMS, email address for EMAIL
    private final String subject;     // ignored by SmsChannel, used by EmailChannel
    private final String body;
    private final String templateName; // which template produced this, for logging

    public Notification(UUID userId, ChannelType channelType, String recipient,
                         String subject, String body, String templateName) {
        this.userId = userId;
        this.channelType = channelType;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.templateName = templateName;
    }

    public UUID getUserId() { return userId; }
    public ChannelType getChannelType() { return channelType; }
    public String getRecipient() { return recipient; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public String getTemplateName() { return templateName; }
}
