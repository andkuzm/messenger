package com.react_spring.messenger.controller;


import com.react_spring.messenger.model.LoginRequest;
import com.react_spring.messenger.system.user.model.User;
import com.react_spring.messenger.system.user.controller.UserController;
import com.react_spring.messenger.system.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private UserService userService;
    private UserController userController;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        userController = new UserController(userService);
    }

    @Test
    void register_ShouldReturnUser() {
        User newUser = new User();
        newUser.setUsername("test");
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("test");

        when(userService.register(newUser)).thenReturn("test");

        ResponseEntity<String> response = userController.register(newUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("test", response.getBody());
    }

    @Test
    void login_ShouldReturnToken() {
        LoginRequest req = new LoginRequest();
        when(userService.login(req)).thenReturn("jwt-token");

        ResponseEntity<String> response = userController.login(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("jwt-token", response.getBody());
    }

    @Test
    void getMe_ShouldReturnUserDetails() {
        Long userId = 99L;
        User user = new User();
        user.setId(userId);
        user.setUsername("alice");

        when(userService.getUserById(userId)).thenReturn(user);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("user", null);
        auth.setDetails(userId);

        ResponseEntity<String[]> response = userController.getMe(auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        String[] body = response.getBody();
        assertNotNull(body);
        assertEquals(userId.toString(), body[0]);
        assertEquals("alice", body[1]);
    }

    @Test
    void updateMe_ShouldReturnUpdatedUser() {
        Long userId = 55L;
        User input = new User();
        input.setUsername("bob");
        User updated = new User();
        updated.setId(userId);
        updated.setUsername("bob");

        when(userService.updateUser(Mockito.any(User.class))).thenReturn(updated);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("user", null);
        auth.setDetails(userId);

        ResponseEntity<User> response = userController.updateMe(input, auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updated, response.getBody());


        verify(userService).updateUser(argThat(u -> u.getId().equals(userId)));
    }

//    @Test
//    void getAllUsers_ShouldReturnList() {
//        User u1 = new User(); u1.setId(1L);
//        User u2 = new User(); u2.setId(2L);
//        List<User> users = Arrays.asList(u1, u2);
//
//        when(userService.getAllUsers()).thenReturn(users);
//
//        ResponseEntity<List<User>> response = userController.getAllUsers();
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals(users, response.getBody());
//    }
}
