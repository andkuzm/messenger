package com.react_spring.messenger.kafka.consumer;

import com.react_spring.messenger.kafka.model.ChatMessage;
import com.react_spring.messenger.service.UnreadService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageConsumer {

    private final UnreadService unreadService;

    public ChatMessageConsumer(UnreadService unreadService) {
        this.unreadService = unreadService;
    }

    @KafkaListener(
            topics = "chat-messages",
            groupId = "chat-group",
            containerFactory = "chatMessageKafkaListenerFactory"
    )
    public void consume(ChatMessage message) {
        System.out.println("Received message: " + message);
        unreadService.increment(message.getChatId(), message.getReceiverId());
    }
}
