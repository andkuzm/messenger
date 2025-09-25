package com.react_spring.messenger.controller;

import com.react_spring.messenger.model.Chat;
import com.react_spring.messenger.service.MessageService;
import com.react_spring.messenger.system.user.model.User;
import com.react_spring.messenger.service.ChatService;
import com.react_spring.messenger.system.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class ChatControllerTest { //TODO

    private ChatService chatService;
    private UserService userService;
    private ChatController chatController;
    private MessageService messageService;

    @BeforeEach
    void setUp() {
        chatService = mock(ChatService.class);
        userService = mock(UserService.class);
        messageService = mock(MessageService.class);
        chatController = new ChatController(chatService, userService, messageService);
    }

    @Test
    void findChatsByUserId_ShouldReturnChats() {
        Long userId = 1L;
        List<Chat> chats = List.of(new Chat());
        when(chatService.getChatsByUsersId(userId)).thenReturn(chats);

        ResponseEntity<Object> response = chatController.findChatsByUserId(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(chats, response.getBody());
        verify(chatService).getChatsByUsersId(userId);
    }

    @Test
    void getChat_ShouldReturnChat_WhenExists() {
        Long chatId = 1L;
        Chat chat = new Chat();
        when(chatService.getChat(chatId)).thenReturn(Optional.of(chat));

        ResponseEntity<Object> response = chatController.getChat(chatId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(chat, response.getBody());
        verify(chatService).getChat(chatId);
    }

    @Test
    void getChat_ShouldReturnNotFound_WhenMissing() {
        Long chatId = 1L;
        when(chatService.getChat(chatId)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = chatController.getChat(chatId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(chatService).getChat(chatId);
    }

    @Test
    void createChat_ShouldReturnChat_WhenCreated() {
        List<Long> userIds = List.of(1L, 2L);
        User u1 = new User(); u1.setId(1L);
        User u2 = new User(); u2.setId(2L);
        Chat chat = new Chat();

        when(userService.getUserById(1L)).thenReturn(u1);
        when(userService.getUserById(2L)).thenReturn(u2);
        when(chatService.createChat(any(Chat.class))).thenReturn(Optional.of(chat));

        ResponseEntity<Object> response = chatController.createChat(userIds, "Test");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(chat, response.getBody());
        verify(chatService).createChat(any(Chat.class));
    }

    @Test
    void createChat_ShouldReturnBadRequest_WhenNotCreated() {
        List<Long> userIds = List.of(1L);
        User u1 = new User(); u1.setId(1L);

        when(userService.getUserById(1L)).thenReturn(u1);
        when(chatService.createChat(any(Chat.class))).thenReturn(Optional.empty());

        ResponseEntity<Object> response = chatController.createChat(userIds, "Test");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(chatService).createChat(any(Chat.class));
    }
}
