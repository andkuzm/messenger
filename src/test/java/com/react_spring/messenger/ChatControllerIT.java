package com.react_spring.messenger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.react_spring.messenger.model.Chat;
import com.react_spring.messenger.model.LoginRequest;
import com.react_spring.messenger.model.Message;
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

        sender = new User();
        sender.setUsername("bob1");
        sender.setPassword("bobPass");

        reader = new User();
        reader.setUsername("alice1");
        reader.setPassword("alicePass");

        chat = new Chat();
        chat.setTitle("TestChat");
        chat = chatRepository.save(chat);

        mockMvc.perform(post("/user/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sender)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/user/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reader)))
                .andExpect(status().isOk());

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
        List<Long> userIds = Arrays.asList(sender.getId(), reader.getId());
        String json = objectMapper.writeValueAsString(userIds);

        mockMvc.perform(post("/chat/create")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
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
}
