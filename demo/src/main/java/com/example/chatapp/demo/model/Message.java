package com.example.chatapp.demo.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
@Table(name = "message")

public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)


    private Long id;
    @JsonAlias({"from_user_id","to_user_id"})

    private Long fromUserId, toUserId;
    private String content;
//    private Instant timestamp;

    public Message() {
    }

    public Message(Long f, Long t, String c) {
        this.fromUserId = f;
        this.toUserId = t;
        this.content = c;
//        this.timestamp = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public String getContent() {
        return content;
    }

//    public Instant getTimestamp() {
//        return timestamp;
//    }
}
