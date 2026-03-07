package com.react_spring.messenger.controller;

import com.react_spring.messenger.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/notification")
@RestController
class NotificationController {

    private final NotificationService notificationService;

    NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/{chatId}")
    ResponseEntity<Integer> getNotifications(@PathVariable Long chatId, Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        return new ResponseEntity<>(notificationService.getCount(chatId, userId), HttpStatus.OK);
    }

    @PutMapping("/{chatId}/reduce")
    ResponseEntity<Void> reduceNotifications(@PathVariable Long chatId, Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        notificationService.reset(chatId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
