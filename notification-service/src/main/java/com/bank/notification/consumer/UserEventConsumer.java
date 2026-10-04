package com.bank.notification.consumer;

import com.bank.notification.event.UserEvent;
import com.bank.notification.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class UserEventConsumer {

    private final NotificationService notificationService;

    public UserEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "user.events", groupId = "notification-service")
    public void onMessage(UserEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", event.getName());

        notificationService.handleUserEvent(
                event.getType(), event.getUserId(), event.getEmail(), event.getPhone(), data, event);
    }
}
