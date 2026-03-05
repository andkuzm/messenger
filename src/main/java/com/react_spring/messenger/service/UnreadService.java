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

    public long getUnread(Long chatId, Long userId) {
        Integer val = redisTemplate.opsForValue().get(KEY_PREFIX + chatId + ":" + userId);
        return val == null ? 0 : val;
    }

    public void increment(Long chatId, Long receiverId) {
        redisTemplate.opsForValue().increment(KEY_PREFIX + chatId + ":" + receiverId);
    }

    /** Sets the unread count absolutely, flooring at 0. */
    public void set(Long chatId, Long userId, int value) {
        redisTemplate.opsForValue().set(KEY_PREFIX + chatId + ":" + userId, Math.max(0, value));
    }
}
