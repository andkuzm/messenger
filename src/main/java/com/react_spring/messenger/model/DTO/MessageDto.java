package com.react_spring.messenger.model.DTO;

import com.react_spring.messenger.system.user.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageDto {
    private User receiver;
    @NotNull
    private Long chatId;
    @NotBlank
    private String message;
}
