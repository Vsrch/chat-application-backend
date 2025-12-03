package com.example.chatapp.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class FriendRequestdto {

    @NotNull(message = "userId is required")
    @Positive(message = "userId must be positive")
    private Long userId;      // sender

    @NotNull(message = "friendId is required")
    @Positive(message = "friendId must be positive")
    private Long friendId;    // receiver

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getFriendId() {
        return friendId;
    }
    public void setFriendId(Long friendId) {
        this.friendId = friendId;
    }
}
