package com.react_spring.messenger.kafka.consumer;

import com.react_spring.messenger.kafka.model.ChatMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageConsumer {

    @KafkaListener(topics = "chat-messages", groupId = "chat-group")
    public void consume(ChatMessage message) {
        System.out.println("Received message: " + message);
        // TODO: persist to Postgres, update Redis, etc.
    }
}
