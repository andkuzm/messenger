package com.react_spring.messenger.controller;

import com.react_spring.messenger.model.Chat;
import com.react_spring.messenger.model.Message;
import com.react_spring.messenger.service.MessageService;
import com.react_spring.messenger.system.user.model.User;
import com.react_spring.messenger.service.ChatService;
import com.react_spring.messenger.system.user.service.UserService;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
     * @return list of all chats that user is in
     */
    @GetMapping("/by-user/{userId}")
    ResponseEntity<Object> findChatsByUserId(@PathVariable Long userId) {
        List<Chat> chats = chatService.getChatsByUsersId(userId);
        return new ResponseEntity<>(chats, HttpStatus.OK); //TODO fallback
    }

    /**
     * Getting a single chat.
     *
     * @param chatId id of the chat to retrieve
     * @return 200 OK and message if successful
     *         404 and exception text if unsuccessful
     */
    @GetMapping("/{chatId}")
    ResponseEntity<Object> getChat(@PathVariable Long chatId) { //TODO validate correct user
        Chat chat = chatService.getChat(chatId).orElse(null);
        if (chat==null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(chat, HttpStatus.OK); //TODO make pagination with gradual approx +-40 messages pull
    }

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
     * Creation of chat
     *
     * @param userIds list of all users of the chat at the moment of its creation
     * @param title title of the chat, can be null
     * @return 200 OK and chat object if successful
     *         400 if unsuccessful
     */
    @PostMapping("/create")
    ResponseEntity<Object> createChat(@RequestBody List<Long> userIds, @Nullable @RequestBody String title) {
        List<User> users = new ArrayList<>();
        for (Long userId : userIds) {
            User user = userService.getUserById(userId);
            users.add(user);
        }
        Chat chat = new Chat();
        chat.setTitle(title);
        chat.setUsers(users);
        Optional<Chat> chatOrNull = chatService.createChat(chat);
        return chatOrNull.
                <ResponseEntity<Object>>map(value ->
                                new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

}
