package com.react_spring.messenger.service;

import com.react_spring.messenger.model.LoginRequest;
import com.react_spring.messenger.system.user.model.User;
import com.react_spring.messenger.system.jwt.service.JwtService;
import com.react_spring.messenger.system.user.repository.UserRepository;
import com.react_spring.messenger.system.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private JwtService jwtService;
    private UserService userService;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        jwtService = mock(JwtService.class);
        userService = new UserService(userRepository, jwtService);
        passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Test
    void testGetUserById() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");

        when(userRepository.findUserById(1L)).thenReturn(user);

        User result = userService.getUserById(1L);

        assertEquals("alice", result.getUsername());
        verify(userRepository).findUserById(1L);
    }

    @Test
    void testUpdateUser() {
        User updated = new User();
        updated.setId(1L);
        updated.setUsername("bob");

        when(userRepository.save(updated)).thenReturn(updated);

        User result = userService.updateUser(updated);

        assertEquals("bob", result.getUsername());
        verify(userRepository).save(updated);
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(new User(), new User()));
        List<User> result = userService.getAllUsers();
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testRegisterEncodesPassword() {
        User user = new User();
        user.setId(1L);
        user.setUsername("charlie");
        user.setPassword("plainPassword");

        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.register(user);

        assertNotNull(saved);
        assertNotEquals("plainPassword", saved.getPassword());
        assertTrue(passwordEncoder.matches("plainPassword", saved.getPassword()));

        verify(userRepository).save(saved);
    }

    @Test
    void testLoginSuccess() {
        User user = new User();
        user.setId(2L);
        user.setUsername("david");
        user.setPassword(passwordEncoder.encode("secret"));

        LoginRequest req = new LoginRequest();
        req.setUsername("david");
        req.setPassword("secret");

        when(userRepository.findUsersByUsername("david")).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        String token = userService.login(req);

        assertEquals("jwt-token", token);
        verify(jwtService).generateToken(user);
    }

    @Test
    void testLoginUserNotFound() {
        LoginRequest req = new LoginRequest();
        req.setUsername("ghost");
        req.setPassword("irrelevant");

        when(userRepository.findUsersByUsername("ghost")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> userService.login(req));
    }

    @Test
    void testLoginInvalidPassword() {
        User user = new User();
        user.setUsername("eve");
        user.setPassword(passwordEncoder.encode("correct"));

        LoginRequest req = new LoginRequest();
        req.setUsername("eve");
        req.setPassword("wrong");

        when(userRepository.findUsersByUsername("eve")).thenReturn(user);

        assertThrows(RuntimeException.class, () -> userService.login(req));
        verify(jwtService, never()).generateToken(any());
    }
}