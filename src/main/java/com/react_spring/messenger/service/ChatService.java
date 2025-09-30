package com.react_spring.messenger.service;

import com.react_spring.messenger.model.Chat;
import com.react_spring.messenger.repository.ChatRepository;
import com.react_spring.messenger.system.user.model.User;
import com.react_spring.messenger.system.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    public ChatService(ChatRepository chatRepository, UserRepository userRepository) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
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

    public boolean joinChat(Chat chat, Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    chat.getUsers().add(user);
                    chatRepository.save(chat);
                    return true;
                })
                .orElse(false);
    }
}
