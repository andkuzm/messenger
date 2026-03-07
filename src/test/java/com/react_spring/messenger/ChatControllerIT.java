package com.react_spring.messenger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.react_spring.messenger.model.Chat;
import com.react_spring.messenger.model.ChatCreationRequest;
import com.react_spring.messenger.model.LoginRequest;
import com.react_spring.messenger.model.Message;
import com.react_spring.messenger.model.RegisterRequest;
import com.react_spring.messenger.repository.MessageRepository;
import com.react_spring.messenger.system.user.model.User;
import com.react_spring.messenger.repository.ChatRepository;
import com.react_spring.messenger.system.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ChatControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MessageRepository messageRepository;

    private User sender;
    private User reader;
    private Chat chat;

    private String token1;

    @BeforeEach
    void setUp() throws Exception {
        chatRepository.deleteAll();
        userRepository.deleteAll();

        chat = new Chat();
        chat.setTitle("TestChat");
        chat = chatRepository.save(chat);

        RegisterRequest senderRegister = new RegisterRequest();
        senderRegister.setUsername("bob1");
        senderRegister.setPassword("bobPass");

        RegisterRequest readerRegister = new RegisterRequest();
        readerRegister.setUsername("alice1");
        readerRegister.setPassword("alicePass");

        mockMvc.perform(post("/user/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(senderRegister)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/user/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(readerRegister)))
                .andExpect(status().isOk());

        sender = userRepository.findUsersByUsername("bob1");
        reader = userRepository.findUsersByUsername("alice1");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("bob1");
        loginRequest.setPassword("bobPass");
        token1 = mockMvc.perform(post("/user/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @AfterEach
    void cleanUp() {
        chatRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testCreateChat() throws Exception {
        // bob1 (authenticated) creates a chat with alice1; controller adds bob1 automatically
        ChatCreationRequest request = new ChatCreationRequest();
        request.setUserNames(List.of("alice1"));
        request.setTitle("NewChat");

        mockMvc.perform(post("/chat/create")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.users.length()").value(2));
    }

    @Test
    @Transactional
    void testFindChatsByUserId() throws Exception {
        User persistedSender = userRepository.findUsersByUsername("bob1");
        User persistedReader = userRepository.findUsersByUsername("alice1");
        chat.setUsers(new ArrayList<>(Arrays.asList(persistedSender, persistedReader)));
        chatRepository.save(chat);

        mockMvc.perform(get("/chat/by-user/" + persistedSender.getId())
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(chat.getId()))
                .andExpect(jsonPath("$[0].title").value("TestChat"));
    }

    @Test
    @Transactional
    void testGetChatById_found() throws Exception {
        User persistedSender = userRepository.findUsersByUsername("bob1");
        User persistedReader = userRepository.findUsersByUsername("alice1");
        chat.setUsers(new ArrayList<>(Arrays.asList(persistedSender, persistedReader)));
        chatRepository.save(chat);

        mockMvc.perform(get("/chat/" + chat.getId())
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(persistedSender.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(chat.getId()))
                .andExpect(jsonPath("$.title").value("TestChat"));
    }

    @Test
    void testGetChatById_notFound() throws Exception {
        mockMvc.perform(get("/chat/999")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sender.getId())))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetMessages_latest() throws Exception {
        User persistedSender = userRepository.findUsersByUsername("bob1");
        User persistedReader = userRepository.findUsersByUsername("alice1");
        chat.setUsers(new ArrayList<>(Arrays.asList(persistedSender, persistedReader)));
        chatRepository.save(chat);

        // create some messages
        Message m1 = new Message();
        m1.setChat(chat);
        m1.setSender(persistedSender);
        m1.setReceiver(persistedReader);
        m1.setMessage("Hello");
        messageRepository.save(m1);

        Message m2 = new Message();
        m2.setChat(chat);
        m2.setSender(persistedReader);
        m2.setReceiver(persistedSender);
        m2.setMessage("Hi back");
        messageRepository.save(m2);

        mockMvc.perform(get("/chat/" + chat.getId() + "/messages")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("Hi back"))
                .andExpect(jsonPath("$[1].message").value("Hello"));
    }

    @Test
    void testGetMessages_before() throws Exception {
        User persistedSender = userRepository.findUsersByUsername("bob1");
        User persistedReader = userRepository.findUsersByUsername("alice1");
        chat.setUsers(new ArrayList<>(Arrays.asList(persistedSender, persistedReader)));
        chatRepository.save(chat);

        Message old = new Message();
        old.setChat(chat);
        old.setSender(persistedSender);
        old.setReceiver(persistedReader);
        old.setMessage("First");
        messageRepository.save(old);

        Message newer = new Message();
        newer.setChat(chat);
        newer.setSender(persistedReader);
        newer.setReceiver(persistedSender);
        newer.setMessage("Second");
        messageRepository.save(newer);

        // request before "newer" → should only return "First"
        mockMvc.perform(get("/chat/" + chat.getId() + "/messages")
                        .param("beforeMessageId", String.valueOf(newer.getId()))
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("First"))
                .andExpect(jsonPath("$.length()").value(1));
    }


    @Test
    void testGetMessages_after() throws Exception {
        User persistedSender = userRepository.findUsersByUsername("bob1");
        User persistedReader = userRepository.findUsersByUsername("alice1");
        chat.setUsers(new ArrayList<>(Arrays.asList(persistedSender, persistedReader)));
        chatRepository.save(chat);

        Message first = new Message();
        first.setChat(chat);
        first.setSender(persistedSender);
        first.setReceiver(persistedReader);
        first.setMessage("First");
        messageRepository.save(first);

        Message second = new Message();
        second.setChat(chat);
        second.setSender(persistedReader);
        second.setReceiver(persistedSender);
        second.setMessage("Second");
        messageRepository.save(second);

        // request after "first" → should only return "Second"
        mockMvc.perform(get("/chat/" + chat.getId() + "/messages")
                        .param("afterMessageId", String.valueOf(first.getId()))
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("Second"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testJoinChat_ShouldReturnOk_WhenChatExists() throws Exception {
        RegisterRequest joinerRegister = new RegisterRequest();
        joinerRegister.setUsername("carol1");
        joinerRegister.setPassword("carolPass");

        mockMvc.perform(post("/user/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinerRegister)))
                .andExpect(status().isOk());

        User persistedJoiner = userRepository.findUsersByUsername("carol1");

        mockMvc.perform(put("/chat/" + chat.getId() + "/join")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(persistedJoiner.getId())))
                .andExpect(status().isOk());
    }

    @Test
    void testJoinChat_ShouldReturnBadRequest_WhenChatNotFound() throws Exception {
        mockMvc.perform(put("/chat/999999/join")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(1L)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void testGetChatById_forbidden_WhenNotMember() throws Exception {
        RegisterRequest joinerRegister = new RegisterRequest();
        joinerRegister.setUsername("carol1");
        joinerRegister.setPassword("carolPass");

        mockMvc.perform(post("/user/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinerRegister)))
                .andExpect(status().isOk());

        User persistedJoiner = userRepository.findUsersByUsername("carol1");

        mockMvc.perform(put("/chat/" + chat.getId() + "/join")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(persistedJoiner.getId())))
                .andExpect(status().isOk());
        // chat has no members — bob1 is not in it → expect 403
        mockMvc.perform(get("/chat/" + chat.getId())
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isForbidden());
    }
}
