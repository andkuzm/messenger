package com.react_spring.messenger.controller;

import com.react_spring.messenger.model.Chat;
import com.react_spring.messenger.model.ChatCreationRequest;
import com.react_spring.messenger.model.Message;
import com.react_spring.messenger.service.MessageService;
import com.react_spring.messenger.system.user.model.User;
import com.react_spring.messenger.service.ChatService;
import com.react_spring.messenger.system.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequestMapping("/chat")
@RestController
class ChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final MessageService messageService;

    public ChatController(ChatService chatService, UserService userService, MessageService messageService) {
        this.chatService = chatService;
        this.userService = userService;
        this.messageService = messageService;
    }

    /**
     * Get list of all chats where a certain user is present.
     *
     * @param userId id of the user that belongs to the searched chats
     * @return list of all chats that user is in, or 500 on error
     */
    @GetMapping("/by-user/{userId}")
    ResponseEntity<Object> findChatsByUserId(@PathVariable Long userId) {
        try {
            List<Chat> chats = chatService.getChatsByUsersId(userId);
            return new ResponseEntity<>(chats, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Getting a single chat. The authenticated user must be a member of the chat.
     *
     * @param chatId id of the chat to retrieve
     * @param authentication current authenticated user
     * @return 200 OK and chat if successful
     *         403 FORBIDDEN if the requesting user is not a member
     *         404 NOT FOUND if chat does not exist
     */
    @GetMapping("/{chatId}")
    ResponseEntity<Object> getChat(@PathVariable Long chatId, Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        Chat chat = chatService.getChat(chatId).orElse(null);
        if (chat == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        boolean isMember = chat.getUsers().stream().anyMatch(u -> u.getId().equals(userId));
        if (!isMember) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(chat, HttpStatus.OK);
    }

    /**
     * Get messages from a chat with optional pagination.
     *
     * @param chatId id of the chat to retrieve messages from
     * @param beforeMessageId optional id of the message, fetch messages before it
     * @param afterMessageId optional id of the message, fetch messages after it
     * @param size maximum number of messages to return (default 40)
     * @return 200 OK with list of messages if successful
     *         404 if chat not found
     */
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<Message>> getMessages(
            @PathVariable Long chatId,
            @RequestParam(required = false) Long beforeMessageId,
            @RequestParam(required = false) Long afterMessageId,
            @RequestParam(defaultValue = "40") int size
    ) {
        List<Message> messages;
        if (beforeMessageId != null) {
            messages = messageService.getMessagesBefore(chatId, beforeMessageId, size);
        } else if (afterMessageId != null) {
            messages = messageService.getMessagesAfter(chatId, afterMessageId, size);
        } else {
            messages = messageService.getLatestMessages(chatId, size);
        }
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    /**
     * Join a chat by adding a user to it.
     *
     * @param chatId id of the chat to join
     * @param userId id of the user joining the chat (in request body)
     * @return 200 OK if the user successfully joins
     *         400 BAD REQUEST if chat not found or join failed
     */
    @PutMapping("/{chatId}/join")
    public ResponseEntity<Object> joinChat(@PathVariable Long chatId, @RequestBody Long userId) {
        return chatService.getChat(chatId).map(chat->{
            chatService.joinChat(chat, userId);
            return new ResponseEntity<>(HttpStatus.OK);
        }).orElse(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    /**
     * Creates a new chat and adds the authenticated user as a member.
     *
     * @param request contains the list of other usernames to include in the chat, and an optional title
     * @param authentication current authenticated user (automatically added as a member)
     * @return 200 OK and chat object if successful
     *         400 BAD REQUEST if any username is not found or chat creation fails
     */
    @PostMapping("/create")
    ResponseEntity<Object> createChat(@Valid @RequestBody ChatCreationRequest request, Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        List<User> users = new ArrayList<>();
        for (String userName : request.getUserNames()) {
            User user = userService.getUserByUsername(userName);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            users.add(user);
        }
        User user = userService.getUserById(userId);
        users.add(user);
        Chat chat = new Chat();
        chat.setTitle(request.getTitle());
        chat.setUsers(users);

        return chatService.createChat(chat)
                .<ResponseEntity<Object>>map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

}
