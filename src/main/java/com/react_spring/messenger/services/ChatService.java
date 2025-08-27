package com.react_spring.messenger.services;

import com.react_spring.messenger.models.Chat;
import com.react_spring.messenger.repositories.ChatRepository;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    private final ChatRepository chatRepository;

    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public List<Chat> getChatsByUsersId(Long userId) {
        return chatRepository.findByUsersId(userId);
    }

    public Optional<Chat> getChat(Long chatId) {
        return chatRepository.findById(chatId);
    }

    public Optional<Chat> createChat(Chat chat) {
        return Optional.of(chatRepository.save(chat));
    }
}
