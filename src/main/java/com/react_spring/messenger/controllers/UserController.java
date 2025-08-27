package com.react_spring.messenger.controllers;

import com.react_spring.messenger.models.LoginRequest;
import com.react_spring.messenger.models.User;
import com.react_spring.messenger.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/user")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Register a user.
     *
     * @param user user to be registered
     * @return 200 OK if successful
     */
    @PostMapping("/auth/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        return ResponseEntity.ok(userService.register(user));
    }

    /**
     * Login with user credentials.
     *
     * @param loginRequest data containing credentials provided by user
     * @return 200 OK and JWT token if successful
     */
    @PostMapping("/auth/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        return new  ResponseEntity<>(userService.login(loginRequest), HttpStatus.OK);
    }

    /**
     * Get information about logged-in user.
     *
     * @param authentication information for validation of logged-in user
     * @return 200 OK and information about user if successful
     */
    @GetMapping("/me")
    public ResponseEntity<String[]> getMe(Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(new String[]{user.getId().toString(), user.getUsername()});
    }

//    // Admin-only endpoint
//    @PreAuthorize("hasRole('ADMIN')")
//    @GetMapping("/all")
//    public ResponseEntity<List<User>> getAllUsers() {
//        return ResponseEntity.ok(userService.getAllUsers());
//    }

    /**
     * update the logged-in user
     *
     * @param updatedUser user to be updated
     * @param authentication information for validation of logged-in user
     * @return 200 OK and updated information about user if successful
     */
    @PutMapping("/me")
    public ResponseEntity<User> updateMe(@RequestBody User updatedUser,
                                         Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        updatedUser.setId(userId);
        User user = userService.updateUser(updatedUser);
        return ResponseEntity.ok(user);
    }
}
