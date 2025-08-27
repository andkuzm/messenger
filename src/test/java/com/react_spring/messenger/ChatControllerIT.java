package com.react_spring.messenger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.react_spring.messenger.models.Chat;
import com.react_spring.messenger.models.LoginRequest;
import com.react_spring.messenger.models.User;
import com.react_spring.messenger.repositories.ChatRepository;
import com.react_spring.messenger.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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

    private User sender;
    private User reader;
    private Chat chat;

    private String token1;

    @BeforeEach
    @Transactional
    void setUp() throws Exception {
        chatRepository.deleteAll();
        userRepository.deleteAll();

        sender = new User();
        sender.setUsername("bob");
        sender.setPassword("bobPass");
        sender = userRepository.save(sender);

        reader = new User();
        reader.setUsername("alice");
        reader.setPassword("alicePass");
        reader = userRepository.save(reader);

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
        loginRequest.setUsername("bob");
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
    void testFindChatsByUserId() throws Exception {
        chat.setUsers(Arrays.asList(sender, reader));
        chatRepository.save(chat);

        mockMvc.perform(get("/chat/by-user/" + sender.getId())
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(chat.getId()))
                .andExpect(jsonPath("$[0].title").value("TestChat"));
    }

    @Test
    void testGetChatById_found() throws Exception {
        chat.setUsers(Arrays.asList(sender, reader));
        chatRepository.save(chat);

        mockMvc.perform(get("/chat/" + chat.getId())
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sender.getId())))
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
}
