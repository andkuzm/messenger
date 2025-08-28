package com.react_spring.messenger.repository;

import com.react_spring.messenger.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByUsersId(Long userId);
}
