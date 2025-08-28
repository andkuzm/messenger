package com.react_spring.messenger.service;

import com.react_spring.messenger.model.Message;
import com.react_spring.messenger.repository.MessageRepository;
import org.springframework.stereotype.Service;

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
}
