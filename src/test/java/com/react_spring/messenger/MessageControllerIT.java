package com.react_spring.messenger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.react_spring.messenger.kafka.producer.ChatMessageProducer;
import com.react_spring.messenger.kafka.producer.ChatReadProducer;
import com.react_spring.messenger.model.Chat;
import com.react_spring.messenger.model.LoginRequest;
import com.react_spring.messenger.model.Message;
import com.react_spring.messenger.system.user.model.User;
import com.react_spring.messenger.repository.ChatRepository;
import com.react_spring.messenger.repository.MessageRepository;
import com.react_spring.messenger.system.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MessageControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @MockitoBean
    private ChatMessageProducer chatMessageProducer;

    @MockitoBean
    private ChatReadProducer chatReadProducer;

    private User sender;
    private User reader;
    private Chat chat;
    private String token1;
    private String token2;

    @BeforeEach
    @Transactional
    void setUp() throws Exception {
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

        loginRequest.setUsername("alice");
        loginRequest.setPassword("alicePass");
        token2 = mockMvc.perform(post("/user/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

    }

    @Test
    void testSendMessage() throws Exception {
        var message = new Message();
        message.setMessage("hello world");
        message.setChat(chat);
        message.setSender(sender);
        message.setReceiver(reader);

        mockMvc.perform(post("/message/send")
                        .header("Authorization", "Bearer " + token1)
                        .principal(() -> String.valueOf(sender.getId())) // provides Authentication.getName()
                        .requestAttr("authenticationDetails", sender.getId()) // trick: used below
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetMessageAndChange() throws Exception {
        var message = new Message();
        message.setMessage("ping");
        message.setChat(chat);
        message.setReceiver(reader);
        message.setSender(sender);

        mockMvc.perform(post("/message/send")
                        .header("Authorization", "Bearer " + token1)
                        .principal(() -> String.valueOf(sender.getId()))
                        .requestAttr("authenticationDetails", sender.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk());

        Long messageId = messageRepository.getFirstByMessage("ping").getId();

        mockMvc.perform(get("/message/{id}", messageId)
                        .header("Authorization", "Bearer " + token2)
                        .principal(() -> String.valueOf(reader.getId()))
                        .requestAttr("authenticationDetails", reader.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("ping"));

        mockMvc.perform(put("/message/change/{id}", messageId)
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"pong\""))
                .andExpect(status().isOk());

        mockMvc.perform(get("/message/{id}", messageId)
                        .header("Authorization", "Bearer " + token2)
                        .principal(() -> String.valueOf(reader.getId()))
                        .requestAttr("authenticationDetails", reader.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("\"pong\""));
    }
}
