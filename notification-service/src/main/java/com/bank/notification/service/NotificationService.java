package com.bank.notification.service;

import com.bank.notification.dispatcher.NotificationDispatcher;
import com.bank.notification.dlt.DeadLetterProducer;
import com.bank.notification.exception.NotificationDeliveryException;
import com.bank.notification.model.ChannelType;
import com.bank.notification.model.Notification;
import com.bank.notification.model.NotificationLog;
import com.bank.notification.model.NotificationStatus;
import com.bank.notification.model.UserContact;
import com.bank.notification.repository.NotificationLogRepository;
import com.bank.notification.repository.UserContactRepository;
import com.bank.notification.template.NotificationTemplate;
import com.bank.notification.template.NotificationTemplateResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;


@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationTemplateResolver templateResolver;
    private final NotificationDispatcher dispatcher;
    private final NotificationLogRepository notificationLogRepository;
    private final UserContactRepository userContactRepository;
    private final DeadLetterProducer deadLetterProducer;

    public NotificationService(NotificationTemplateResolver templateResolver,
                                NotificationDispatcher dispatcher,
                                NotificationLogRepository notificationLogRepository,
                                UserContactRepository userContactRepository,
                                DeadLetterProducer deadLetterProducer) {
        this.templateResolver = templateResolver;
        this.dispatcher = dispatcher;
        this.notificationLogRepository = notificationLogRepository;
        this.userContactRepository = userContactRepository;
        this.deadLetterProducer = deadLetterProducer;
    }


    @Transactional
    public void handleUserEvent(String eventType, UUID userId, String email, String phone,
                                 Map<String, Object> data, Object rawPayload) {
        upsertContact(userId, email, phone);
        processEvent(eventType, userId, data, rawPayload);
    }

    public void handleAccountEvent(String eventType, UUID userId, Map<String, Object> data, Object rawPayload) {
        processEvent(eventType, userId, data, rawPayload);
    }

    public void handleTransactionEvent(String eventType, UUID userId, Map<String, Object> data, Object rawPayload) {
        processEvent(eventType, userId, data, rawPayload);
    }

    private void upsertContact(UUID userId, String email, String phone) {
        UserContact contact = userContactRepository.findById(userId)
                .orElse(new UserContact(userId, email, phone));
        contact.update(email, phone);
        userContactRepository.save(contact);
    }

    private void processEvent(String eventType, UUID userId, Map<String, Object> data, Object rawPayload) {
        Optional<NotificationTemplate> template = templateResolver.resolve(eventType);
        if (template.isEmpty()) {
            // e.g. TransactionInitiated — a real event, just not one anyone gets notified about
            log.debug("No notification template registered for event type '{}', skipping", eventType);
            return;
        }

        Optional<UserContact> contact = userContactRepository.findById(userId);
        if (contact.isEmpty()) {
            log.warn("No contact info cached for user {}, sending '{}' to DLT", userId, eventType);
            deadLetterProducer.publish(userId.toString(), rawPayload, "No cached contact info for user");
            return;
        }

        Notification notification = buildNotification(template.get(), userId, contact.get(), data);
        deliverAndLog(notification, rawPayload);
    }

    private Notification buildNotification(NotificationTemplate template, UUID userId,
                                             UserContact contact, Map<String, Object> data) {
        Notification draft = template.build(userId, null, data);
        String recipient = draft.getChannelType() == ChannelType.SMS ? contact.getPhone() : contact.getEmail();
        // rebuild with the correct recipient now that we know which channel the template picked
        return new Notification(userId, draft.getChannelType(), recipient,
                draft.getSubject(), draft.getBody(), draft.getTemplateName());
    }

    private void deliverAndLog(Notification notification, Object rawPayload) {
        NotificationStatus status;
        try {
            dispatcher.dispatch(notification);
            status = NotificationStatus.SENT;
        } catch (NotificationDeliveryException ex) {
            log.error("Failed to deliver notification via {}: {}", notification.getChannelType(), ex.getMessage());
            status = NotificationStatus.FAILED;
            deadLetterProducer.publish(notification.getUserId().toString(), rawPayload, ex.getMessage());
        }
        notificationLogRepository.save(NotificationLog.of(notification, status));
    }
}
