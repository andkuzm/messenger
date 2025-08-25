package com.react_spring.messenger.controllers;

import com.react_spring.messenger.models.Chat;
import com.react_spring.messenger.services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RequestMapping("/chat")
@RestController
class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/by-user/{userId}")
    ResponseEntity<Object> findChatsByUserId(@PathVariable Long userId) {
        List<Chat> chats = chatService.getChatsByUsersId(userId);
        return new ResponseEntity<>(chats, HttpStatus.OK);
    }

    @GetMapping("/{chatId}")
    ResponseEntity<Object> getChat(@PathVariable Long chatId, @RequestBody Long userId) {
        Chat chat = chatService.getChat(chatId).orElse(null);
        if (chat==null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(chat, HttpStatus.OK); //TODO make pagination with gradual approx +-40 messages pull
    }

}
