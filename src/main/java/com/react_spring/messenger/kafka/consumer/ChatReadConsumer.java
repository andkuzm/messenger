package com.react_spring.messenger.kafka.consumer;

import com.react_spring.messenger.kafka.model.ChatRead;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ChatReadConsumer {

    private final RedisTemplate<String, Integer> redisTemplate;

    public ChatReadConsumer(RedisTemplate<String, Integer> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(
            topics = "chat-read",
            groupId = "chat-group",
            containerFactory = "chatReadKafkaListenerFactory"
    )
    public void consume(ChatRead message) {
        System.out.println("read message: " + message);

        String key = "unread:" + message.getChatId() + ":" + message.getReaderId(); //key: "unread:{message.getChatId()}:{message.getReaderId()}"
        redisTemplate.opsForValue().decrement(key);
    }
}