package com.react_spring.messenger.model;

import lombok.Data;

import java.util.List;

@Data
public class ChatCreationRequest {
    private List<String> userNames;
    private String title;
}
