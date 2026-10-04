package com.bank.notification.template;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AccountCreditedTemplate extends NotificationTemplate {

    @Override
    public boolean supports(String eventType) {
        return "AccountCredited".equals(eventType);
    }

    @Override
    protected String buildSubject(Map<String, Object> data) {
        return "Credit alert";
    }

    @Override
    protected String buildBody(Map<String, Object> data) {
        return "Your account was credited with " + data.get("amount") + ".";
    }
}
