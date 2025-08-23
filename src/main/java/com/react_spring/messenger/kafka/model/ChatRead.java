package com.react_spring.messenger.kafka.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRead {
    private Long chatId;
    private Long senderId;
    private Long readerId;
    private Integer unreadRemains;
}
