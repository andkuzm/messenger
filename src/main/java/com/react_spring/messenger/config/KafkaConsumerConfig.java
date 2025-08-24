package com.react_spring.messenger.config;

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
import org.springframework.kafka.support.serializer.JsonDeserializer; //TODO to be deprecated, find fix for JacksonJson tools. lib
import com.fasterxml.jackson.databind.ObjectMapper;

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
        ObjectMapper objectMapper = new ObjectMapper();
        JsonDeserializer<ChatMessage> deserializer = new JsonDeserializer<>(ChatMessage.class, objectMapper, false);
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
        ObjectMapper objectMapper = new ObjectMapper();
        JsonDeserializer<ChatRead> deserializer = new JsonDeserializer<>(ChatRead.class, objectMapper, false);
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
