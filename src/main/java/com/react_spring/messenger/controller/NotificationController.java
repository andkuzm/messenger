package com.react_spring.messenger.controller;

import com.react_spring.messenger.service.UnreadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/notification")
@RestController
class NotificationController {

    private final UnreadService unreadService;

    NotificationController(UnreadService unreadService) {
        this.unreadService = unreadService;
    }

    @GetMapping("/{chatId}")
    ResponseEntity<Integer> getNotifications(@PathVariable Long chatId, Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        return new ResponseEntity<>(unreadService.getCount(chatId, userId), HttpStatus.OK);
    }

    @PutMapping("/{chatId}/reduce")
    ResponseEntity<Void> reduceNotifications(@PathVariable Long chatId, Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        unreadService.reset(chatId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
