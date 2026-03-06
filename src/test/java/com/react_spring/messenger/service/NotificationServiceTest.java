package com.react_spring.messenger.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock
    private RedisTemplate<String, Integer> redisTemplate;

    @Mock
    private ValueOperations<String, Integer> valueOps;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void getCount_returnsValueFromRedis() {
        when(valueOps.get("unread:1:2")).thenReturn(5);

        int result = notificationService.getCount(1L, 2L);

        assertEquals(5, result);
        verify(valueOps).get("unread:1:2");
    }

    @Test
    void getCount_returnsZeroWhenKeyAbsent() {
        when(valueOps.get("unread:1:2")).thenReturn(null);

        int result = notificationService.getCount(1L, 2L);

        assertEquals(0, result);
    }

    @Test
    void increment_callsRedisIncrement() {
        notificationService.increment(1L, 2L);

        verify(valueOps).increment("unread:1:2");
    }

    @Test
    void decrement_callsRedisDecrement() {
        notificationService.decrement(1L, 2L);

        verify(valueOps).decrement("unread:1:2");
    }

    @Test
    void reset_setsValueToZero() {
        notificationService.reset(1L, 2L);

        verify(valueOps).set("unread:1:2", 0);
    }
}
