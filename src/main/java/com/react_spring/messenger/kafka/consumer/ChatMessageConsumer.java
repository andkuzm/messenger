package com.react_spring.messenger.kafka.consumer;

import com.react_spring.messenger.kafka.model.ChatMessage;
import com.react_spring.messenger.service.UnreadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageConsumer {

    private final UnreadService unreadService;

    @KafkaListener(
            topics = "${kafka.topic.chat-messages}",
            groupId = "chat-message-group",
            containerFactory = "chatMessageKafkaListenerFactory"
    )
    public void consume(ChatMessage message) {
        try {
            log.debug("Received message event: {}", message);
            unreadService.increment(message.getChatId(), message.getReceiverId());
        } catch (Exception e) {
            log.error("Error processing chat-messages event: {}", message, e);
        }
    }
}
