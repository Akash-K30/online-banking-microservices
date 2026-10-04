package com.bank.notification.consumer;

import com.bank.notification.event.TransactionEvent;
import com.bank.notification.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TransactionEventConsumer {

    private final NotificationService notificationService;

    public TransactionEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "transaction.events", groupId = "notification-service")
    public void onMessage(TransactionEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("transactionId", event.getTransactionId());
        if (event.getAmount() != null) {
            data.put("amount", event.getAmount());
        }
        if (event.getReason() != null) {
            data.put("reason", event.getReason());
        }

        notificationService.handleTransactionEvent(event.getType(), event.getFromUserId(), data, event);
    }
}
