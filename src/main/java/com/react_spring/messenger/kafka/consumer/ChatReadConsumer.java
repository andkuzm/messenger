package com.react_spring.messenger.kafka.consumer;

import com.react_spring.messenger.kafka.model.ChatRead;
import com.react_spring.messenger.service.UnreadService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ChatReadConsumer {

    private final UnreadService unreadService;

    public ChatReadConsumer(UnreadService unreadService) {
        this.unreadService = unreadService;
    }

    @KafkaListener(
            topics = "chat-read",
            groupId = "chat-group",
            containerFactory = "chatReadKafkaListenerFactory"
    )
    public void consume(ChatRead message) {
        System.out.println("read message: " + message);
        unreadService.decrement(message.getChatId(), message.getReaderId());
    }
}
