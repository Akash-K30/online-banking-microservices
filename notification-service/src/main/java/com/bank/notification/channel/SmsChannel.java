package com.bank.notification.channel;

import com.bank.notification.model.ChannelType;
import com.bank.notification.model.Notification;
import org.springframework.stereotype.Component;


@Component
public class SmsChannel implements NotificationChannel {

    @Override
    public void send(Notification notification) {
        System.out.printf("[SMS -> %s] %s%n", notification.getRecipient(), notification.getBody());
    }

    @Override
    public ChannelType getType() {
        return ChannelType.SMS;
    }
}
