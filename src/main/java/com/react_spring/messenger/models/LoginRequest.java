package com.react_spring.messenger.models;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
