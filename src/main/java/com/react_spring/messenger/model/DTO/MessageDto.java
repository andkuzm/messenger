package com.react_spring.messenger.model.DTO;

import com.react_spring.messenger.system.user.model.User;
import lombok.Data;

@Data
public class MessageDto {
    private User sender;
    private User receiver;
    private Long chatId;
    private String message;
}
