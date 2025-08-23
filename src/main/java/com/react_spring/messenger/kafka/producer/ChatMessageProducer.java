package com.react_spring.messenger.kafka.producer;

import com.react_spring.messenger.kafka.model.ChatMessage;
import com.react_spring.messenger.models.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatMessageProducer {

    private final KafkaTemplate<String, ChatMessage> kafkaTemplate;

    private static final String TOPIC = "chat-messages";

    public void sendMessage(ChatMessage message) {
        kafkaTemplate.send(TOPIC, message.getChatId().toString(), message);
    }

    public ChatMessage convertToKafkaMessage(Message message) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setChatId(message.getChat().getId());
        chatMessage.setSenderId(message.getSender().getId());
        chatMessage.setReceiverId(message.getReceiver().getId());
        chatMessage.setContent(message.getMessage());
        chatMessage.setTimestamp(message.getTimestamp());
        return  chatMessage;
    }
}
