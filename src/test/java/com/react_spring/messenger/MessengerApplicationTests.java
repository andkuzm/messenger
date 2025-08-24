package com.react_spring.messenger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.react_spring.messenger.models.LoginRequest;
import com.react_spring.messenger.models.Message;
import com.react_spring.messenger.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MessengerApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	// 1. Register user
	@Test
	void testRegisterUser() throws Exception {
		User user = new User();
		user.setLogin("testuser");
		user.setHashedPassword("secret");

		mockMvc.perform(post("/users/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(user)))
				.andExpect(status().isOk());
	}

	// 2. Login should return JWT
	@Test
	void testLoginUser() throws Exception {
		LoginRequest login = new LoginRequest();
		login.setUsername("testuser");
		login.setPassword("secret");

		mockMvc.perform(post("/users/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(login)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isString()); // token returned
	}

	// 3. Access protected endpoint without token = forbidden
	@Test
	void testProtectedWithoutTokenFails() throws Exception {
		mockMvc.perform(get("/messages/1"))
				.andExpect(status().isForbidden());
	}

	// 4. Send message with token (you can inject JWT directly)
	@Test
	void testSendMessage() throws Exception {
		// Normally: obtain JWT from login response
		String jwt = "Bearer " + "<insert valid token>";

		Message msg = new Message();
		msg.setMessage("Hello");
		// assume you have chatId set correctly

		mockMvc.perform(post("/messages/send")
						.header("Authorization", jwt)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(msg)))
				.andExpect(status().isOk());
	}
}
