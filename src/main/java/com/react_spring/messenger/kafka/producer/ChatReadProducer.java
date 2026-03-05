package com.react_spring.messenger.kafka.producer;

import com.react_spring.messenger.kafka.model.ChatRead;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatReadProducer {

    private final KafkaTemplate<String, ChatRead> kafkaTemplate;

    @Value("${kafka.topic.chat-read}")
    private String topic;

    public void sendMessage(ChatRead message) {
        kafkaTemplate.send(topic, message.getChatId().toString(), message);
    }
}
