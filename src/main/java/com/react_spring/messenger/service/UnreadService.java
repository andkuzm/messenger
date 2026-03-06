package com.react_spring.messenger.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UnreadService {

    private static final String KEY_PREFIX = "unread:";

    private final RedisTemplate<String, Integer> redisTemplate;

    public UnreadService(RedisTemplate<String, Integer> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String key(Long chatId, Long userId) {
        return KEY_PREFIX + chatId + ":" + userId;
    }

    public int getCount(Long chatId, Long userId) {
        Integer count = redisTemplate.opsForValue().get(key(chatId, userId));
        return count != null ? count : 0;
    }

    public void increment(Long chatId, Long userId) {
        redisTemplate.opsForValue().increment(key(chatId, userId));
    }

    public void decrement(Long chatId, Long userId) {
        redisTemplate.opsForValue().decrement(key(chatId, userId));
    }

    public void reset(Long chatId, Long userId) {
        redisTemplate.opsForValue().set(key(chatId, userId), 0);
    }
}
