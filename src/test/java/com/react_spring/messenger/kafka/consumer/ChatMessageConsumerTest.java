package com.react_spring.messenger.kafka.consumer;

import com.react_spring.messenger.kafka.model.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatMessageConsumerTest { //TODO

    @Mock
    private RedisTemplate<String, Integer> redisTemplate;

    @Mock
    private ValueOperations<String, Integer> valueOps;

    @InjectMocks
    private ChatMessageConsumer consumer;

    @BeforeEach
    void setup() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void testConsume() {
        ChatMessage msg = new ChatMessage();
        msg.setChatId(1L);
        msg.setReceiverId(2L);

        consumer.consume(msg);

        verify(valueOps).increment("unread:1:2");
    }
}
