package com.react_spring.messenger.service;

import com.react_spring.messenger.model.Message;
import com.react_spring.messenger.repository.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Message getMessageById(Long id) {
        return messageRepository.getReferenceById(id);
    }

    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }

    public Message ChangeMessageById(Long id, String newText) {
        Message message = messageRepository.findById(id).orElseThrow();
        message.setMessage(newText);
        return messageRepository.save(message);
    }

    public List<Message> getLatestMessages(Long chatId, int size) {
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return messageRepository.findByChatId(chatId, pageable).getContent();
    }

    public List<Message> getMessagesBefore(Long chatId, Long beforeMessageId, int size) {
        Message before = messageRepository.findById(beforeMessageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return messageRepository.findByChatIdAndTimestampBefore(chatId, before.getTimestamp(), pageable).getContent();
    }

    public List<Message> getMessagesAfter(Long chatId, Long afterMessageId, int size) {
        Message after = messageRepository.findById(afterMessageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.ASC, "timestamp"));
        return messageRepository.findByChatIdAndTimestampAfter(chatId, after.getTimestamp(), pageable).getContent();
    }
}
