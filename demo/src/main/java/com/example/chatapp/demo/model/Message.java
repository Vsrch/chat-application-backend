package com.example.chatapp.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "message")
@Data
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long fromUserId;
    private Long toUserId;
    private String content;

    @Column(updatable = false)
    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        this.sentAt = LocalDateTime.now();
    }

    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;

    public Message() {}

    public Message(Long from, Long to, String c) {
        this.fromUserId = from;
        this.toUserId = to;
        this.content = c;
        this.sentAt = LocalDateTime.now();
    }
}
