package com.react_spring.messenger.repositories;

import com.react_spring.messenger.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Message getMessageById(Long id);

    Message getFirstByMessage(String message);
}
