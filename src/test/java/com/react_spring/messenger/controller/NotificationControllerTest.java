package com.react_spring.messenger.controller;

import com.react_spring.messenger.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class NotificationControllerTest {

    private NotificationService notificationService;
    private NotificationController notificationController;

    @BeforeEach
    void setUp() {
        notificationService = mock(NotificationService.class);
        notificationController = new NotificationController(notificationService);
    }

    @Test
    void getNotifications_returnsCountForUser() {
        Long chatId = 1L;
        Long userId = 42L;
        Authentication auth = mock(Authentication.class);
        when(auth.getDetails()).thenReturn(userId);
        when(notificationService.getCount(chatId, userId)).thenReturn(7);

        ResponseEntity<Integer> response = notificationController.getNotifications(chatId, auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(7, response.getBody());
        verify(notificationService).getCount(chatId, userId);
    }

    @Test
    void getNotifications_returnsZeroWhenNone() {
        Long chatId = 5L;
        Long userId = 10L;
        Authentication auth = mock(Authentication.class);
        when(auth.getDetails()).thenReturn(userId);
        when(notificationService.getCount(chatId, userId)).thenReturn(0);

        ResponseEntity<Integer> response = notificationController.getNotifications(chatId, auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody());
    }

    @Test
    void reduceNotifications_resetsCountAndReturnsOk() {
        Long chatId = 1L;
        Long userId = 42L;
        Authentication auth = mock(Authentication.class);
        when(auth.getDetails()).thenReturn(userId);

        ResponseEntity<Void> response = notificationController.reduceNotifications(chatId, auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(notificationService).reset(chatId, userId);
    }
}
