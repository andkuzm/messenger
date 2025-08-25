package com.react_spring.messenger.controllers;

import com.react_spring.messenger.kafka.model.ChatMessage;
import com.react_spring.messenger.kafka.model.ChatRead;
import com.react_spring.messenger.kafka.producer.ChatMessageProducer;
import com.react_spring.messenger.kafka.producer.ChatReadProducer;
import com.react_spring.messenger.models.Message;
import com.react_spring.messenger.models.User;
import com.react_spring.messenger.services.MessageService;
import com.react_spring.messenger.services.UserService;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/message")
@RestController
class MessageController {

    private final MessageService messageService;
    private final ChatMessageProducer chatMessageProducer;
    private final ChatReadProducer chatReadProducer;
    private final UserService userService;

    MessageController(MessageService messageService, ChatMessageProducer chatMessageProducer, ChatReadProducer chatReadProducer, UserService userService) {
        this.messageService = messageService;
        this.chatMessageProducer = chatMessageProducer;
        this.chatReadProducer = chatReadProducer;
        this.userService = userService;
    }

    @GetMapping("/change/{messageId}")
    ResponseEntity<Object> ChangeMessage(@PathVariable Long messageId, @RequestBody String content) {
        Message resp = messageService.ChangeMessageById(messageId, content);
        if (resp != null) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>("Message editing attempt unsuccessful", HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/{messageId}")
    ResponseEntity<Object> GetMessage(@PathVariable Long messageId, Authentication authentication) {
        try {
            Message resp = messageService.getMessageById(messageId);

            ChatRead cr = new ChatRead();
            cr.setChatId(resp.getChat().getId());
            cr.setSenderId(resp.getSender().getId());
            cr.setReaderId((Long) authentication.getDetails()); //TODO get curretn authorized user
            chatReadProducer.sendMessage(cr);

            return new ResponseEntity<>(resp, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/send")
    ResponseEntity<Object> sendMessage(@RequestBody Message message, Authentication authentication) {
        try {
            User trueSender = userService.getUserById((Long) authentication.getDetails());
            message.setSender(trueSender);
            Message resp = messageService.saveMessage(message);
            if (resp != null) {
                chatMessageProducer.sendMessage(
                        chatMessageProducer.convertToKafkaMessage(resp)
                );
                return new ResponseEntity<>(HttpStatus.OK);
            }
            else{
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
