package com.react_spring.messenger.system.user.service;

import com.react_spring.messenger.model.LoginRequest;
import com.react_spring.messenger.model.RegisterRequest;
import com.react_spring.messenger.system.user.model.User;
import com.react_spring.messenger.system.jwt.service.JwtService;
import com.react_spring.messenger.system.user.repository.UserRepository;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    public UserService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public User getUserById(Long id){
        return userRepository.findUserById(id);
    }

    public User updateUser(User updatedUser) {
        return userRepository.save(updatedUser);
    }

    public @Nullable List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public @Nullable String login(LoginRequest loginRequest) {
        User user = userRepository.findUsersByUsername(loginRequest.getUsername());
        if(user == null){
            throw new UsernameNotFoundException("User not found");
        }

        if(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return jwtService.generateToken(user);
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }

    public @Nullable String register(RegisterRequest registerRequest) {
        User user = new User();
        String hashed = passwordEncoder.encode(registerRequest.getPassword());
        user.setPassword(hashed);
        user.setUsername(registerRequest.getUsername());
        userRepository.save(user);
        return user.getUsername();
    }
}
