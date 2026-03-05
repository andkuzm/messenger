package com.react_spring.messenger.kafka.consumer;

import com.react_spring.messenger.kafka.model.ChatRead;
import com.react_spring.messenger.service.UnreadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatReadConsumer {

    private final UnreadService unreadService;

    @KafkaListener(
            topics = "chat-read",
            groupId = "chat-read-group",
            containerFactory = "chatReadKafkaListenerFactory"
    )
    public void consume(ChatRead message) {
        try {
            log.debug("Read event: {}", message);
            // Use the pre-calculated remaining count from the event for an atomic, floor-safe SET
            int remaining = message.getUnreadRemains() != null ? message.getUnreadRemains() : 0;
            unreadService.set(message.getChatId(), message.getReaderId(), remaining);
        } catch (Exception e) {
            log.error("Error processing chat-read event: {}", message, e);
        }
    }
}
