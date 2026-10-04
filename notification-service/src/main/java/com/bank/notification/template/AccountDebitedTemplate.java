package com.bank.notification.template;

import com.bank.notification.model.ChannelType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AccountDebitedTemplate extends NotificationTemplate {

    @Override
    public boolean supports(String eventType) {
        return "AccountDebited".equals(eventType);
    }

    @Override
    protected String buildSubject(Map<String, Object> data) {
        return "Debit alert";
    }

    @Override
    protected String buildBody(Map<String, Object> data) {
        return "Your account was debited by " + data.get("amount") + ".";
    }

    @Override
    protected ChannelType preferredChannel(Map<String, Object> data) {
        // money-moving-out alerts go over SMS: faster and more likely to be seen immediately
        return ChannelType.SMS;
    }
}
