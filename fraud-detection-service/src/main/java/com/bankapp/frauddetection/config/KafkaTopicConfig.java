package com.bankapp.frauddetection.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * We produce `fraud.verdicts`, so we declare it here (partitioned, per the
 * partitioning tip in section 1.3, so verdicts for one transaction always land
 * on the same partition and stay ordered). `transaction.events` is owned and
 * declared by the Transaction Service - we only consume it.
 */
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic fraudVerdictsTopic(
            @Value("${app.kafka.topics.fraud-verdicts}") String topicName,
            @Value("${app.kafka.fraud-verdicts-partitions}") int partitions,
            @Value("${app.kafka.fraud-verdicts-replication-factor}") short replicationFactor) {
        return TopicBuilder.name(topicName)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }
}
