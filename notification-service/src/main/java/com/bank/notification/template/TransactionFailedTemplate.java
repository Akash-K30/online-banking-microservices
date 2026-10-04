package com.bank.notification.template;

import com.bank.notification.model.ChannelType;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
public class TransactionFailedTemplate extends NotificationTemplate {

    @Override
    public boolean supports(String eventType) {
        return "TransactionFailed".equals(eventType);
    }

    @Override
    protected String buildSubject(Map<String, Object> data) {
        return "FRAUD".equals(data.get("reason")) ? "Suspicious transfer blocked" : "Transfer failed";
    }

    @Override
    protected String buildBody(Map<String, Object> data) {
        if ("FRAUD".equals(data.get("reason"))) {
            return "We blocked a transfer on your account (" + data.get("transactionId")
                    + ") because it looked suspicious. If this was you, contact support.";
        }
        return "Transaction " + data.get("transactionId") + " failed: " + data.get("reason");
    }

    @Override
    protected ChannelType preferredChannel(Map<String, Object> data) {
        return ChannelType.SMS; // failures/fraud are always urgent
    }
}
