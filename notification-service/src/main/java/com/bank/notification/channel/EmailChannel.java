package com.bank.notification.channel;

import com.bank.notification.model.ChannelType;
import com.bank.notification.model.Notification;
import org.springframework.stereotype.Component;


@Component
public class EmailChannel implements NotificationChannel {

    @Override
    public void send(Notification notification) {
        System.out.printf("[EMAIL -> %s] Subject: %s%n%s%n",
                notification.getRecipient(), notification.getSubject(), notification.getBody());
    }

    @Override
    public ChannelType getType() {
        return ChannelType.EMAIL;
    }
}
