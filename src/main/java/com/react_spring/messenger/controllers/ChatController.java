package com.react_spring.messenger.controllers;

import com.react_spring.messenger.models.Chat;
import com.react_spring.messenger.models.User;
import com.react_spring.messenger.services.ChatService;
import com.react_spring.messenger.services.UserService;
import jakarta.annotation.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequestMapping("/chat")
@RestController
class ChatController {

    private final ChatService chatService;
    private final UserService userService;

    public ChatController(ChatService chatService, UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    @GetMapping("/by-user/{userId}")
    ResponseEntity<Object> findChatsByUserId(@PathVariable Long userId) {
        List<Chat> chats = chatService.getChatsByUsersId(userId);
        return new ResponseEntity<>(chats, HttpStatus.OK);
    }


    @GetMapping("/{chatId}")
    ResponseEntity<Object> getChat(@PathVariable Long chatId) { //validate correct user
        Chat chat = chatService.getChat(chatId).orElse(null);
        if (chat==null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(chat, HttpStatus.OK); //TODO make pagination with gradual approx +-40 messages pull
    }

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
