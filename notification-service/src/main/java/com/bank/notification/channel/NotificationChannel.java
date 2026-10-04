package com.bank.notification.channel;

import com.bank.notification.exception.NotificationDeliveryException;
import com.bank.notification.model.ChannelType;
import com.bank.notification.model.Notification;


public interface NotificationChannel {

    void send(Notification notification) throws NotificationDeliveryException;

    ChannelType getType();
}
