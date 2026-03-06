package com.react_spring.messenger.kafka.consumer;

import com.react_spring.messenger.kafka.model.ChatMessage;
import com.react_spring.messenger.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatMessageConsumerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ChatMessageConsumer consumer;

    @Test
    void consume_incrementsNotificationForReceiver() {
        ChatMessage msg = new ChatMessage();
        msg.setChatId(1L);
        msg.setReceiverId(2L);

        consumer.consume(msg);

        verify(notificationService).increment(1L, 2L);
    }
}
