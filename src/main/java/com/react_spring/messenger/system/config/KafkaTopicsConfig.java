package com.react_spring.messenger.system.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicsConfig {

    @Value("${kafka.topic.chat-messages}")
    private String chatMessagesTopic;

    @Value("${kafka.topic.chat-read}")
    private String chatReadTopic;

    @Value("${kafka.topic.partitions}")
    private int partitions;

    @Value("${kafka.topic.replication-factor}")
    private short replicationFactor;

    @Bean
    public NewTopic chatMessagesTopic() {
        return new NewTopic(chatMessagesTopic, partitions, replicationFactor);
    }

    @Bean
    public NewTopic chatReadTopic() {
        return new NewTopic(chatReadTopic, partitions, replicationFactor);
    }
}
