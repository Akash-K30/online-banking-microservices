package com.bank.notification.dispatcher;

import com.bank.notification.channel.NotificationChannel;
import com.bank.notification.exception.NotificationDeliveryException;
import com.bank.notification.model.ChannelType;
import com.bank.notification.model.Notification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
public class NotificationDispatcher {

    private final Map<ChannelType, NotificationChannel> channels;

    public NotificationDispatcher(List<NotificationChannel> channelList) {
        this.channels = channelList.stream()
                .collect(Collectors.toMap(NotificationChannel::getType, Function.identity()));
    }

    public void dispatch(Notification notification) {
        NotificationChannel channel = channels.get(notification.getChannelType());
        if (channel == null) {
            throw new NotificationDeliveryException(
                    "No NotificationChannel registered for type " + notification.getChannelType());
        }
        channel.send(notification);
    }
}
