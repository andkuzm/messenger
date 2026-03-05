package com.react_spring.messenger.controller;

import com.react_spring.messenger.kafka.model.ChatRead;
import com.react_spring.messenger.kafka.producer.ChatMessageProducer;
import com.react_spring.messenger.kafka.producer.ChatReadProducer;
import com.react_spring.messenger.model.Chat;
import com.react_spring.messenger.model.DTO.MessageDto;
import com.react_spring.messenger.model.Message;
import com.react_spring.messenger.service.ChatService;
import com.react_spring.messenger.service.MessageService;
import com.react_spring.messenger.service.UnreadService;
import com.react_spring.messenger.system.user.model.User;
import com.react_spring.messenger.system.user.service.UserService;
import jakarta.validation.Valid;
import org.hibernate.ObjectNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RequestMapping("/message")
@RestController
class MessageController {

    private final MessageService messageService;
    private final ChatMessageProducer chatMessageProducer;
    private final ChatReadProducer chatReadProducer;
    private final UserService userService;
    private final ChatService chatService;
    private final UnreadService unreadService;

    MessageController(MessageService messageService, ChatMessageProducer chatMessageProducer,
                      ChatReadProducer chatReadProducer, UserService userService,
                      ChatService chatService, UnreadService unreadService) {
        this.messageService = messageService;
        this.chatMessageProducer = chatMessageProducer;
        this.chatReadProducer = chatReadProducer;
        this.userService = userService;
        this.chatService = chatService;
        this.unreadService = unreadService;
    }

    /**
     * Content update for an existing message. Only the original sender may edit.
     *
     * @param messageId id of the message to be changed
     * @param content new content for the message
     * @param authentication current authenticated user (must be the sender)
     * @return 200 if message changed successfully
     *         403 FORBIDDEN if the requester is not the sender
     *         400 if change attempt was unsuccessful
     */
    @PutMapping("/change/{messageId}")
    ResponseEntity<Object> changeMessage(@PathVariable Long messageId, @RequestBody String content,
                                         Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        Message resp = messageService.changeMessageById(messageId, content, userId);
        if (resp != null) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>("Message editing attempt unsuccessful", HttpStatus.BAD_REQUEST);
    }

    /**
     * Get a single message. The authenticated user must be a member of the chat.
     *
     * @param messageId message to get
     * @param authentication current authenticated user
     * @return 200 OK and message if successful
     *         403 FORBIDDEN if user is not a chat member
     *         404 if message not found
     */
    @GetMapping("/{messageId}")
    ResponseEntity<Object> getMessage(@PathVariable Long messageId, Authentication authentication) {
        try {
            Long userId = (Long) authentication.getDetails();
            Message resp = messageService.getMessageById(messageId);

            // Issue 4: verify requester is a member of the message's chat
            Long chatId = resp.getChat().getId();
            Optional<Chat> chatOpt = chatService.getChat(chatId);
            if (chatOpt.isEmpty() || chatOpt.get().getUsers().stream().noneMatch(u -> u.getId().equals(userId))) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            // Issue 8: populate unreadRemains before publishing the read event
            int unreadRemains = (int) Math.max(0, unreadService.getUnread(chatId, userId) - 1);

            ChatRead cr = new ChatRead();
            cr.setChatId(chatId);
            cr.setSenderId(resp.getSender().getId());
            cr.setReaderId(userId);
            cr.setUnreadRemains(unreadRemains);
            chatReadProducer.sendMessage(cr);

            return new ResponseEntity<>(resp, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Handles sending of the message.
     *
     * @param messageDto Message dataObject to send
     * @param authentication current authenticated user
     * @return 200 OK and message if successful
     *         404 if chat not found
     */
    @PostMapping("/send")
    ResponseEntity<Object> sendMessage(@Valid @RequestBody MessageDto messageDto, Authentication authentication) {
        try {
            Message message = new Message();
            User trueSender = userService.getUserById((Long) authentication.getDetails());
            message.setSender(trueSender);
            message.setReceiver(messageDto.getReceiver());
            Optional<Chat> chat = chatService.getChat(messageDto.getChatId());
            if (chat.isEmpty()) {
                throw new ObjectNotFoundException(messageDto.getChatId(), "chat not found");
            }
            message.setChat(chat.get());
            message.setMessage(messageDto.getMessage());
            Message resp = messageService.saveMessage(message);

            if (resp != null) {
                chatMessageProducer.sendMessage(
                        chatMessageProducer.convertToKafkaMessage(resp)
                );
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
