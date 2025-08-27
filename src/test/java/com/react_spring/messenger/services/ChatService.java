package com.react_spring.messenger.services;

import com.react_spring.messenger.models.Chat;
import com.react_spring.messenger.repositories.ChatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @InjectMocks
    private ChatService chatService;

    private Chat chat;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        chat = new Chat();
        chat.setId(1L);
        chat.setTitle("Test Chat");
    }

    @Test
    void testGetChatsByUsersId() {
        when(chatRepository.findByUsersId(1L)).thenReturn(List.of(chat));

        List<Chat> chats = chatService.getChatsByUsersId(1L);

        assertEquals(1, chats.size());
        assertEquals("Test Chat", chats.get(0).getTitle());
        verify(chatRepository, times(1)).findByUsersId(1L);
    }

    @Test
    void testGetChatFound() {
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chat));

        Optional<Chat> result = chatService.getChat(1L);

        assertTrue(result.isPresent());
        assertEquals("Test Chat", result.get().getTitle());
        verify(chatRepository, times(1)).findById(1L);
    }

    @Test
    void testGetChatNotFound() {
        when(chatRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<Chat> result = chatService.getChat(2L);

        assertFalse(result.isPresent());
        verify(chatRepository, times(1)).findById(2L);
    }

    @Test
    void testCreateChat() {
        when(chatRepository.save(chat)).thenReturn(chat);

        Optional<Chat> result = chatService.createChat(chat);

        assertTrue(result.isPresent());
        assertEquals("Test Chat", result.get().getTitle());
        verify(chatRepository, times(1)).save(chat);
    }
}
