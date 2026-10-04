package com.bank.notification.template;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;


@Component
public class NotificationTemplateResolver {

    private final List<NotificationTemplate> templates;

    public NotificationTemplateResolver(List<NotificationTemplate> templates) {
        this.templates = templates;
    }

    public Optional<NotificationTemplate> resolve(String eventType) {
        return templates.stream().filter(t -> t.supports(eventType)).findFirst();
    }
}
