package com.react_spring.messenger.service;

import com.react_spring.messenger.model.Message;
import com.react_spring.messenger.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MessageServiceTest {

    private MessageRepository messageRepository;
    private MessageService messageService;

    @BeforeEach
    void setUp() {
        messageRepository = mock(MessageRepository.class);
        messageService = new MessageService(messageRepository);
    }

    @Test
    void testGetMessageById() {
        Message message = new Message();
        message.setId(1L);
        message.setMessage("hello");

        when(messageRepository.getReferenceById(1L)).thenReturn(message);

        Message result = messageService.getMessageById(1L);

        assertNotNull(result);
        assertEquals("hello", result.getMessage());
        verify(messageRepository, times(1)).getReferenceById(1L);
    }

    @Test
    void testSaveMessage() {
        Message message = new Message();
        message.setMessage("hi");

        when(messageRepository.save(message)).thenReturn(message);

        Message result = messageService.saveMessage(message);

        assertNotNull(result);
        assertEquals("hi", result.getMessage());
        verify(messageRepository, times(1)).save(message);
    }

    @Test
    void testChangeMessageById() {
        Message message = new Message();
        message.setId(1L);
        message.setMessage("old");

        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        Message result = messageService.ChangeMessageById(1L, "new text");

        assertEquals("new text", result.getMessage());


        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        assertEquals("new text", captor.getValue().getMessage());
    }

    @Test
    void testChangeMessageByIdNotFound() {
        when(messageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                messageService.ChangeMessageById(99L, "text")
        );

        verify(messageRepository, never()).save(any());
    }
}