package com.react_spring.messenger.kafka.consumer;

import com.react_spring.messenger.kafka.model.ChatMessage;
import com.react_spring.messenger.kafka.model.ChatRead;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ChatReadConsumer {

    @KafkaListener(topics = "chat-read", groupId = "chat-group")
    public void consume(ChatRead message) {
        System.out.println("red message: " + message);
        // TODO: persist to Postgres, update Redis, etc.
    }
}