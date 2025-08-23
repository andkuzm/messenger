package com.react_spring.messenger.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicsConfig {

    @Bean
    public NewTopic chatMessagesTopic() {
        return new NewTopic("chat-messages", 3, (short) 3);
    }

    @Bean
    public NewTopic chatReadTopic() {
        return new NewTopic("chat-read", 3, (short) 3);
    }
}
