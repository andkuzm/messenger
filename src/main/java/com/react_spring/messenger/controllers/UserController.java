package com.react_spring.messenger.controllers;

import com.react_spring.messenger.models.LoginRequest;
import com.react_spring.messenger.models.User;
import com.react_spring.messenger.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Public endpoint: registration or login
    @PostMapping("/auth/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        return ResponseEntity.ok(userService.register(user));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(userService.login(loginRequest));
    }

    // Protected endpoint: user can see their own profile
    @GetMapping("/me")
    public ResponseEntity<User> getMe(Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    // Admin-only endpoint
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Example: user can update only their own info
    @PutMapping("/me")
    public ResponseEntity<User> updateMe(@RequestBody User updatedUser,
                                         Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        updatedUser.setId(userId);
        User user = userService.updateUser(updatedUser);
        return ResponseEntity.ok(user);
    }
}
