package com.react_spring.messenger.controller;


import com.react_spring.messenger.kafka.model.ChatMessage;
import com.react_spring.messenger.kafka.model.ChatRead;
import com.react_spring.messenger.kafka.producer.ChatMessageProducer;
import com.react_spring.messenger.kafka.producer.ChatReadProducer;
import com.react_spring.messenger.model.Chat;
import com.react_spring.messenger.model.Message;
import com.react_spring.messenger.system.user.model.User;
import com.react_spring.messenger.service.MessageService;
import com.react_spring.messenger.system.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MessageControllerTest { //TODO

    private MessageService messageService;
    private ChatMessageProducer chatMessageProducer;
    private ChatReadProducer chatReadProducer;
    private UserService userService;
    private MessageController messageController;

    @BeforeEach
    void setUp() {
        messageService = mock(MessageService.class);
        chatMessageProducer = mock(ChatMessageProducer.class);
        chatReadProducer = mock(ChatReadProducer.class);
        userService = mock(UserService.class);

        messageController = new MessageController(
                messageService, chatMessageProducer, chatReadProducer, userService
        );
    }

    @Test
    void changeMessage_ShouldReturnOk_WhenMessageChanged() {
        Long messageId = 1L;
        String newContent = "new text";
        Message updated = new Message();

        when(messageService.ChangeMessageById(messageId, newContent)).thenReturn(updated);

        ResponseEntity<Object> response = messageController.ChangeMessage(messageId, newContent);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(messageService).ChangeMessageById(messageId, newContent);
    }

    @Test
    void changeMessage_ShouldReturnBadRequest_WhenMessageNull() {
        Long messageId = 1L;
        String newContent = "new text";

        when(messageService.ChangeMessageById(messageId, newContent)).thenReturn(null);

        ResponseEntity<Object> response = messageController.ChangeMessage(messageId, newContent);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Message editing attempt unsuccessful", response.getBody());
    }

    @Test
    void getMessage_ShouldReturnMessageAndSendReadEvent() {
        Long messageId = 1L;
        Message msg = new Message();
        Chat chat = new Chat(); chat.setId(99L);
        User sender = new User(); sender.setId(42L);
        msg.setChat(chat);
        msg.setSender(sender);

        when(messageService.getMessageById(messageId)).thenReturn(msg);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("user", null);
        auth.setDetails(123L); // authenticated user id

        ResponseEntity<Object> response = messageController.GetMessage(messageId, auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(msg, response.getBody());

        // Verify ChatRead was sent
        ArgumentCaptor<ChatRead> captor = ArgumentCaptor.forClass(ChatRead.class);
        verify(chatReadProducer).sendMessage(captor.capture());
        ChatRead cr = captor.getValue();
        assertEquals(99L, cr.getChatId());
        assertEquals(42L, cr.getSenderId());
        assertEquals(123L, cr.getReaderId());
    }

    @Test
    void getMessage_ShouldReturnNotFound_WhenExceptionThrown() {
        Long messageId = 1L;
        when(messageService.getMessageById(messageId))
                .thenThrow(new RuntimeException("Not found"));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("user", null);
        auth.setDetails(123L);

        ResponseEntity<Object> response = messageController.GetMessage(messageId, auth);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", response.getBody());
    }

    @Test
    void sendMessage_ShouldReturnOk_WhenSaved() {
        Message msg = new Message();
        User sender = new User(); sender.setId(123L);
        Message saved = new Message();
        ChatMessage  chatMessage = new ChatMessage();

        when(userService.getUserById(123L)).thenReturn(sender);
        when(messageService.saveMessage(msg)).thenReturn(saved);
        when(chatMessageProducer.convertToKafkaMessage(saved)).thenReturn(chatMessage);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("user", null);
        auth.setDetails(123L);

        ResponseEntity<Object> response = messageController.sendMessage(msg, auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(chatMessageProducer).sendMessage(chatMessage);
    }

    @Test
    void sendMessage_ShouldReturnNotFound_WhenSaveFails() {
        Message msg = new Message();
        User sender = new User(); sender.setId(123L);

        when(userService.getUserById(123L)).thenReturn(sender);
        when(messageService.saveMessage(msg)).thenReturn(null);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("user", null);
        auth.setDetails(123L);

        ResponseEntity<Object> response = messageController.sendMessage(msg, auth);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void sendMessage_ShouldReturnNotFound_WhenExceptionThrown() {
        Message msg = new Message();
        when(userService.getUserById(anyLong())).thenThrow(new RuntimeException("User missing"));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("user", null);
        auth.setDetails(123L);

        ResponseEntity<Object> response = messageController.sendMessage(msg, auth);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User missing", response.getBody());
    }
}
