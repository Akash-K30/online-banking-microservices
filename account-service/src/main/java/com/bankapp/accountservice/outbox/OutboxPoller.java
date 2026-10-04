package com.bankapp.accountservice.outbox;

import com.bankapp.accountservice.model.OutboxEvent;
import com.bankapp.accountservice.model.OutboxStatus;
import com.bankapp.accountservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPoller {

    private static final String TOPIC = "account.events";

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishPending() {
        List<OutboxEvent> pending = outboxEventRepository
                .findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        for (OutboxEvent event : pending) {
            try {
                // Key by aggregateId (accountId) so all events for one
                // account land on the same partition and stay ordered.
                kafkaTemplate.send(TOPIC, event.getAggregateId().toString(), event.getPayload()).get();
                event.setStatus(OutboxStatus.SENT);
                outboxEventRepository.save(event);
            } catch (Exception e) {
                log.error("Failed to publish outbox event {} - will retry next poll", event.getId(), e);
                // Leave status PENDING; next scheduled run retries it.
            }
        }
    }
}
