package com.bankapp.frauddetection.kafka;

import com.bankapp.frauddetection.dto.FraudVerdict;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Component
public class FraudVerdictProducer {

    private static final Logger log = LoggerFactory.getLogger(FraudVerdictProducer.class);

    private final KafkaTemplate<String, FraudVerdict> kafkaTemplate;
    private final String topic;

    public FraudVerdictProducer(KafkaTemplate<String, FraudVerdict> kafkaTemplate,
                                 @Value("${app.kafka.topics.fraud-verdicts}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(FraudVerdict verdict) {
        String key = verdict.getTransactionId().toString();
        kafkaTemplate.send(topic, key, verdict).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish fraud verdict for transaction {}", verdict.getTransactionId(), ex);
            } else {
                log.debug("Published {} verdict for transaction {} to {}",
                        verdict.getVerdict(), verdict.getTransactionId(), topic);
            }
        });
    }
}
