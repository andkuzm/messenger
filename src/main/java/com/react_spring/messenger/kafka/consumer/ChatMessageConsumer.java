package com.react_spring.messenger.kafka.consumer;

import com.react_spring.messenger.kafka.model.ChatMessage;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageConsumer {

    private final RedisTemplate<String, Integer> redisTemplate;

    public ChatMessageConsumer(RedisTemplate<String, Integer> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = "chat-messages", groupId = "chat-group")
    public void consume(ChatMessage message) {
        System.out.println("Received message: " + message);
        String key = "unread:" + message.getChatId() + ":" + message.getReceiverId(); //key: "unread:{message.getChatId()}:{message.getReaderId()}"
        redisTemplate.opsForValue().increment(key);
    }
}
