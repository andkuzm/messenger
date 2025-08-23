package com.react_spring.messenger.kafka.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private Long chatId;
    private Long senderId;
    private Long receiverId;
    private String content;
    private Timestamp timestamp;
}
