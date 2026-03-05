package com.react_spring.messenger.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ChatCreationRequest {
    @NotEmpty
    private List<String> userNames;
    private String title;
}
