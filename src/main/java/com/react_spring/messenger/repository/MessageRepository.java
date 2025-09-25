package com.react_spring.messenger.repository;

import com.react_spring.messenger.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.net.ContentHandler;
import java.sql.Timestamp;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Message getMessageById(Long id);

    Message getFirstByMessage(String message);

    Page<Message> findByChatIdOrderByTimestamp(Long id, Pageable pageable);

    Page<Message> findByChatId(Long chatId, Pageable pageable);

    Page<Message> findByChatIdAndTimestampBefore(Long chatId, Timestamp timestamp, Pageable pageable);

    Page<Message> findByChatIdAndTimestampAfter(Long chatId, Timestamp timestamp, Pageable pageable);
}
