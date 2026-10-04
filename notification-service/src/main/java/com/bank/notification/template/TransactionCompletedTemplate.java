package com.bank.notification.template;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TransactionCompletedTemplate extends NotificationTemplate {

    @Override
    public boolean supports(String eventType) {
        return "TransactionCompleted".equals(eventType);
    }

    @Override
    protected String buildSubject(Map<String, Object> data) {
        return "Transfer completed";
    }

    @Override
    protected String buildBody(Map<String, Object> data) {
        return "Transaction " + data.get("transactionId") + " for " + data.get("amount") + " completed successfully.";
    }
}
