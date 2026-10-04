package com.bank.notification.consumer;

import com.bank.notification.event.AccountEvent;
import com.bank.notification.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AccountEventConsumer {

    private final NotificationService notificationService;

    public AccountEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "account.events", groupId = "notification-service")
    public void onMessage(AccountEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("accountId", event.getAccountId());
        if (event.getAmount() != null) {
            data.put("amount", event.getAmount());
        }
        if (event.getReason() != null) {
            data.put("reason", event.getReason());
        }

        notificationService.handleAccountEvent(event.getType(), event.getUserId(), data, event);
    }
}
