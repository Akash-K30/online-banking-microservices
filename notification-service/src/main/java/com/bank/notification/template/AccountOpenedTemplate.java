package com.bank.notification.template;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AccountOpenedTemplate extends NotificationTemplate {

    @Override
    public boolean supports(String eventType) {
        return "AccountOpened".equals(eventType);
    }

    @Override
    protected String buildSubject(Map<String, Object> data) {
        return "Your new account is ready";
    }

    @Override
    protected String buildBody(Map<String, Object> data) {
        return "Account " + data.get("accountId") + " has been opened and is ready to use.";
    }
}
