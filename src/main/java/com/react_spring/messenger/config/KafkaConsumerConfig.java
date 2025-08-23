package com.react_spring.messenger.config;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.react_spring.messenger.kafka.model.ChatMessage;
import com.react_spring.messenger.kafka.model.ChatRead;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    private Map<String, Object> consumerConfigs(String groupId) {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092,localhost:29094,localhost:29096");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return config;
    }

    @Bean
    public ConsumerFactory<String, ChatMessage> chatMessageConsumerFactory() {
        JacksonJsonDeserializer<ChatMessage> deserializer = new JacksonJsonDeserializer<>(ChatMessage.class);
        deserializer.addTrustedPackages("*"); //TODO mb more specific
        return new DefaultKafkaConsumerFactory<>(
                consumerConfigs("chat-message-group"), new StringDeserializer(), deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChatMessage> chatMessageKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ChatMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(chatMessageConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, ChatRead> chatReadConsumerFactory() {
        JacksonJsonDeserializer<ChatRead> deserializer = new JacksonJsonDeserializer<>(ChatRead.class);
        deserializer.addTrustedPackages("*");
        return new DefaultKafkaConsumerFactory<>(
                consumerConfigs("chat-read-group"), new StringDeserializer(), deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChatRead> chatReadKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ChatRead> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(chatReadConsumerFactory());
        return factory;
    }
}
