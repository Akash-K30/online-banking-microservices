package com.bank.notification.template;

import com.bank.notification.model.ChannelType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AccountFrozenTemplate extends NotificationTemplate {

    @Override
    public boolean supports(String eventType) {
        return "AccountFrozen".equals(eventType);
    }

    @Override
    protected String buildSubject(Map<String, Object> data) {
        return "Your account has been frozen";
    }

    @Override
    protected String buildBody(Map<String, Object> data) {
        return "Your account was frozen. Reason: " + data.get("reason") + ". Contact support for help.";
    }

    @Override
    protected ChannelType preferredChannel(Map<String, Object> data) {
        return ChannelType.SMS; // account freezes are urgent
    }
}
