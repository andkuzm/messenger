package com.react_spring.messenger.kafka.consumer;

import com.react_spring.messenger.kafka.model.ChatMessage;
import com.react_spring.messenger.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageConsumer {

    private final NotificationService notificationService;

    public ChatMessageConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(
            topics = "chat-messages",
            groupId = "chat-group",
            containerFactory = "chatMessageKafkaListenerFactory"
    )
    public void consume(ChatMessage message) {
        System.out.println("Received message: " + message);
        notificationService.increment(message.getChatId(), message.getReceiverId());
    }
}
