package com.bank.notification.template;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserRegisteredTemplate extends NotificationTemplate {

    @Override
    public boolean supports(String eventType) {
        return "UserRegistered".equals(eventType);
    }

    @Override
    protected String buildSubject(Map<String, Object> data) {
        return "Welcome to the bank!";
    }

    @Override
    protected String buildBody(Map<String, Object> data) {
        return "Hi " + data.get("name") + ", your account has been created successfully.";
    }
}
