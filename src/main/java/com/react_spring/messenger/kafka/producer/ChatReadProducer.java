package com.react_spring.messenger.kafka.producer;

import com.react_spring.messenger.kafka.model.ChatMessage;
import com.react_spring.messenger.kafka.model.ChatRead;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatReadProducer {

    private final KafkaTemplate<String, ChatRead> kafkaTemplate;

    private static final String TOPIC = "chat-read";

    public void sendMessage(ChatRead message) {
        kafkaTemplate.send(TOPIC, message.getChatId().toString(), message);
    }
}
