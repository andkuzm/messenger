package com.react_spring.messenger.kafka.consumer;

import com.react_spring.messenger.kafka.model.ChatRead;
import com.react_spring.messenger.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ChatReadConsumer {

    private final NotificationService notificationService;

    public ChatReadConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(
            topics = "chat-read",
            groupId = "chat-group",
            containerFactory = "chatReadKafkaListenerFactory"
    )
    public void consume(ChatRead message) {
        System.out.println("read message: " + message);
        notificationService.decrement(message.getChatId(), message.getReaderId());
    }
}
