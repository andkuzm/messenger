package com.react_spring.messenger.model;

import com.react_spring.messenger.system.user.model.User;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "messages")
@Data
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @UpdateTimestamp
    private Timestamp timestamp;

    private String message;
}
