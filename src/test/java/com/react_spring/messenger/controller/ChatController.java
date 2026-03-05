package com.react_spring.messenger.controller;

import com.react_spring.messenger.model.Chat;
import com.react_spring.messenger.model.ChatCreationRequest;
import com.react_spring.messenger.model.Message;
import com.react_spring.messenger.service.MessageService;
import com.react_spring.messenger.system.user.model.User;
import com.react_spring.messenger.service.ChatService;
import com.react_spring.messenger.system.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChatControllerTest {

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
    void findChatsByUserId_ShouldReturnInternalServerError_WhenExceptionThrown() {
        Long userId = 1L;
        when(chatService.getChatsByUsersId(userId)).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<Object> response = chatController.findChatsByUserId(userId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void getChat_ShouldReturnChat_WhenMemberExists() {
        Long chatId = 1L;
        Long userId = 42L;
        User member = new User();
        member.setId(userId);
        Chat chat = new Chat();
        chat.setUsers(new ArrayList<>(List.of(member)));

        Authentication authentication = mock(Authentication.class);
        when(authentication.getDetails()).thenReturn(userId);
        when(chatService.getChat(chatId)).thenReturn(Optional.of(chat));

        ResponseEntity<Object> response = chatController.getChat(chatId, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(chat, response.getBody());
        verify(chatService).getChat(chatId);
    }

    @Test
    void getChat_ShouldReturnForbidden_WhenNotMember() {
        Long chatId = 1L;
        Long userId = 42L;
        User otherUser = new User();
        otherUser.setId(99L);
        Chat chat = new Chat();
        chat.setUsers(new ArrayList<>(List.of(otherUser)));

        Authentication authentication = mock(Authentication.class);
        when(authentication.getDetails()).thenReturn(userId);
        when(chatService.getChat(chatId)).thenReturn(Optional.of(chat));

        ResponseEntity<Object> response = chatController.getChat(chatId, authentication);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getChat_ShouldReturnNotFound_WhenMissing() {
        Long chatId = 1L;
        Authentication authentication = mock(Authentication.class);
        when(authentication.getDetails()).thenReturn(42L);
        when(chatService.getChat(chatId)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = chatController.getChat(chatId, authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(chatService).getChat(chatId);
    }

    @Test
    void createChat_ShouldReturnChat_WhenCreated() {
        ChatCreationRequest request = new ChatCreationRequest();
        request.setUserNames(List.of("alice"));
        request.setTitle("TestChat");

        User alice = new User(); alice.setId(2L); alice.setUsername("alice");
        User creator = new User(); creator.setId(1L); creator.setUsername("bob");
        Chat chat = new Chat();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getDetails()).thenReturn(1L);
        when(userService.getUserByUsername("alice")).thenReturn(alice);
        when(userService.getUserById(1L)).thenReturn(creator);
        when(chatService.createChat(any(Chat.class))).thenReturn(Optional.of(chat));

        ResponseEntity<Object> response = chatController.createChat(request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(chat, response.getBody());
        verify(chatService).createChat(any(Chat.class));
    }

    @Test
    void createChat_ShouldReturnBadRequest_WhenUserNotFound() {
        ChatCreationRequest request = new ChatCreationRequest();
        request.setUserNames(List.of("nonexistent"));
        request.setTitle("TestChat");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getDetails()).thenReturn(1L);
        when(userService.getUserByUsername("nonexistent")).thenReturn(null);

        ResponseEntity<Object> response = chatController.createChat(request, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createChat_ShouldReturnBadRequest_WhenCreationFails() {
        ChatCreationRequest request = new ChatCreationRequest();
        request.setUserNames(List.of("alice"));
        request.setTitle("TestChat");

        User alice = new User(); alice.setId(2L); alice.setUsername("alice");
        User creator = new User(); creator.setId(1L);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getDetails()).thenReturn(1L);
        when(userService.getUserByUsername("alice")).thenReturn(alice);
        when(userService.getUserById(1L)).thenReturn(creator);
        when(chatService.createChat(any(Chat.class))).thenReturn(Optional.empty());

        ResponseEntity<Object> response = chatController.createChat(request, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testGetLatestMessages() {
        Long chatId = 1L;
        int size = 40;
        List<Message> expectedMessages = List.of(new Message(), new Message());

        when(messageService.getLatestMessages(chatId, size)).thenReturn(expectedMessages);

        ResponseEntity<List<Message>> response = chatController.getMessages(chatId, null, null, size);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedMessages, response.getBody());
        verify(messageService).getLatestMessages(chatId, size);
        verifyNoMoreInteractions(messageService);
    }

    @Test
    void testGetMessagesBefore() {
        Long chatId = 1L;
        Long beforeMessageId = 10L;
        int size = 20;
        List<Message> expectedMessages = List.of(new Message());

        when(messageService.getMessagesBefore(chatId, beforeMessageId, size)).thenReturn(expectedMessages);

        ResponseEntity<List<Message>> response = chatController.getMessages(chatId, beforeMessageId, null, size);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedMessages, response.getBody());
        verify(messageService).getMessagesBefore(chatId, beforeMessageId, size);
        verifyNoMoreInteractions(messageService);
    }

    @Test
    void testGetMessagesAfter() {
        Long chatId = 1L;
        Long afterMessageId = 5L;
        int size = 15;
        List<Message> expectedMessages = List.of(new Message(), new Message(), new Message());

        when(messageService.getMessagesAfter(chatId, afterMessageId, size)).thenReturn(expectedMessages);

        ResponseEntity<List<Message>> response = chatController.getMessages(chatId, null, afterMessageId, size);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedMessages, response.getBody());
        verify(messageService).getMessagesAfter(chatId, afterMessageId, size);
        verifyNoMoreInteractions(messageService);
    }

    @Test
    void testPreferBeforeOverAfter() {
        Long chatId = 1L;
        Long beforeMessageId = 99L;
        Long afterMessageId = 100L;
        int size = 10;
        List<Message> expectedMessages = List.of(new Message());

        when(messageService.getMessagesBefore(chatId, beforeMessageId, size)).thenReturn(expectedMessages);

        ResponseEntity<List<Message>> response = chatController.getMessages(chatId, beforeMessageId, afterMessageId, size);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedMessages, response.getBody());
        verify(messageService).getMessagesBefore(chatId, beforeMessageId, size);
        verifyNoMoreInteractions(messageService);
    }
}
