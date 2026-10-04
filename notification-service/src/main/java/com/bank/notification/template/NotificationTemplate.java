package com.bank.notification.template;

import com.bank.notification.model.ChannelType;
import com.bank.notification.model.Notification;

import java.util.Map;
import java.util.UUID;


public abstract class NotificationTemplate {

    public final Notification build(UUID userId, String recipient, Map<String, Object> data) {
        String subject = buildSubject(data);
        String body = buildBody(data);
        ChannelType channel = preferredChannel(data);
        return new Notification(userId, channel, recipient, subject, body, getClass().getSimpleName());
    }

    /** Which upstream event type(s) this template turns into a message, e.g. "UserRegistered". */
    public abstract boolean supports(String eventType);

    protected abstract String buildSubject(Map<String, Object> data);

    protected abstract String buildBody(Map<String, Object> data);

    /** Hook method — override only when a notification needs to skip the EMAIL default. */
    protected ChannelType preferredChannel(Map<String, Object> data) {
        return ChannelType.EMAIL;
    }
}
