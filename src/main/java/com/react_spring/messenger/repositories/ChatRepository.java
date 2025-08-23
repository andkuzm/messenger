package com.react_spring.messenger.repositories;

import com.react_spring.messenger.models.Chat;
import com.react_spring.messenger.models.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByUsersId(Long userId);
}
