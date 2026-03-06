package com.react_spring.messenger.controller;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/notification")
@RestController
class NotificationController {

    private final RedisTemplate<String, Integer> redisTemplate;

    NotificationController(RedisTemplate<String, Integer> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Get the number of unread notifications for the authenticated user in a given chat.
     *
     * @param chatId the chat to check
     * @param authentication the current authenticated user
     * @return 200 and the unread count (0 if none)
     */
    @GetMapping("/{chatId}")
    ResponseEntity<Integer> getNotifications(@PathVariable Long chatId, Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        String key = "unread:" + chatId + ":" + userId;
        Integer count = redisTemplate.opsForValue().get(key);
        return new ResponseEntity<>(count != null ? count : 0, HttpStatus.OK);
    }

    /**
     * Reset the unread notification count to zero for the authenticated user in a given chat.
     *
     * @param chatId the chat whose notifications should be cleared
     * @param authentication the current authenticated user
     * @return 200 OK
     */
    @PutMapping("/{chatId}/reduce")
    ResponseEntity<Void> reduceNotifications(@PathVariable Long chatId, Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        String key = "unread:" + chatId + ":" + userId;
        redisTemplate.opsForValue().set(key, 0);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
