package com.react_spring.messenger.controller;

import com.react_spring.messenger.kafka.model.ChatRead;
import com.react_spring.messenger.kafka.producer.ChatMessageProducer;
import com.react_spring.messenger.kafka.producer.ChatReadProducer;
import com.react_spring.messenger.model.Message;
import com.react_spring.messenger.system.user.model.User;
import com.react_spring.messenger.service.MessageService;
import com.react_spring.messenger.system.user.service.UserService;
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

    /**
     * Content update for an existing message.
     *
     * @param messageId id of the message to be changed
     * @param content new content for the message
     * @return 200 if message changed successfully
     *         400 if change attempt was unsuccessful
     */
    @PutMapping("/change/{messageId}")
    ResponseEntity<Object> ChangeMessage(@PathVariable Long messageId, @RequestBody String content) {
        Message resp = messageService.ChangeMessageById(messageId, content);
        if (resp != null) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>("Message editing attempt unsuccessful", HttpStatus.BAD_REQUEST);
    }

    /**
     * Get a single message.
     * @param messageId message to get
     * @param authentication authentithication for validation of request
     * @return 200 OK and message if successful
     *         404 and exception text if unsuccessful
     */
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

    /**
     * Handles sending of the message.
     * @param message Message object to send
     * @param authentication authentithication for validation of request
     * @return 200 OK and message if successful
     *         404 and exception text if unsuccessful
     *         404 and exception text if unsuccessful with runtime exception triggered
     */
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
