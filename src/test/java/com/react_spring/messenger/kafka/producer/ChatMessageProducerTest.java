package com.react_spring.messenger.kafka.producer;

import com.react_spring.messenger.kafka.model.ChatMessage;
import com.react_spring.messenger.models.Chat;
import com.react_spring.messenger.models.Message;
import com.react_spring.messenger.models.User;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatMessageProducerTest { //TODO

    @Mock
    private KafkaTemplate<String, ChatMessage> kafkaTemplate;

    @InjectMocks
    private ChatMessageProducer producer;

    @Test
    void testSendMessage() {
        ChatMessage msg = new ChatMessage();
        msg.setChatId(1L);

        producer.sendMessage(msg);

        verify(kafkaTemplate).send("chat-messages", "1", msg);
    }

    @Test
    void testConvertToKafkaMessage() {
        Message entity = new Message();
        entity.setMessage("hi");
        User sender = new User(); sender.setId(1L);
        User receiver = new User(); receiver.setId(2L);
        Chat chat = new Chat(); chat.setId(99L);
        entity.setSender(sender);
        entity.setReceiver(receiver);
        entity.setChat(chat);

        ChatMessage chatMessage = producer.convertToKafkaMessage(entity);

        assertEquals(99L, chatMessage.getChatId());
        assertEquals(1L, chatMessage.getSenderId());
        assertEquals(2L, chatMessage.getReceiverId());
        assertEquals("hi", chatMessage.getContent());
    }
}