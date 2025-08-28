package com.react_spring.messenger;

import com.react_spring.messenger.kafka.model.ChatMessage;
import com.react_spring.messenger.kafka.model.ChatRead;
import com.react_spring.messenger.kafka.producer.ChatMessageProducer;
import com.react_spring.messenger.kafka.producer.ChatReadProducer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@Transactional
@SpringBootTest
class ChatKafkaIT {

    @Autowired
    private ChatMessageProducer messageProducer;

    @Autowired
    private ChatReadProducer readProducer;

    @Autowired
    private RedisTemplate<String, Integer> redisTemplate;

    private final String key = "unread:1:2";

    @BeforeEach
    void setup() {
        // Option 1: delete just the test key
        redisTemplate.delete(key);

        // Option 2: flush all Redis data (good for isolation, but slower)
        // redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    void testMessageAndReadFlow() {

        // --- 1. Send message ---
        ChatMessage msg = new ChatMessage();
        msg.setChatId(1L);
        msg.setSenderId(1L);
        msg.setReceiverId(2L);

        messageProducer.sendMessage(msg);

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .until(() -> redisTemplate.opsForValue().get(key) != null);

        assertEquals(1, redisTemplate.opsForValue().get(key));

        // --- 2. Send read event ---
        ChatRead read = new ChatRead();
        read.setChatId(1L);
        read.setReaderId(2L);

        readProducer.sendMessage(read);

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .until(() -> redisTemplate.opsForValue().get(key) != null
                        && redisTemplate.opsForValue().get(key) == 0);

        assertEquals(0, redisTemplate.opsForValue().get(key));
    }
}

