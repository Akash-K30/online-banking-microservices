package com.bank.notification.template;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KycUpdatedTemplate extends NotificationTemplate {

    @Override
    public boolean supports(String eventType) {
        return "KycUpdated".equals(eventType);
    }

    @Override
    protected String buildSubject(Map<String, Object> data) {
        return "KYC details updated";
    }

    @Override
    protected String buildBody(Map<String, Object> data) {
        return "Your KYC information was updated. If this wasn't you, contact support immediately.";
    }
}
