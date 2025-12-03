package com.example.chatapp.demo.service;

import com.example.chatapp.demo.model.Friend;
import com.example.chatapp.demo.repository.FriendRepository;
import com.example.chatapp.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    public FriendService(FriendRepository friendRepository,
                         UserRepository userRepository) {
        this.friendRepository = friendRepository;
        this.userRepository = userRepository;
    }

    // ---------------- SEND FRIEND REQUEST ----------------
    public Friend sendRequest(Long senderId, Long receiverId) {

        if (senderId == null || receiverId == null) {
            throw new IllegalArgumentException("userId and friendId are required.");
        }

        // Prevent sending to self
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("You cannot send a friend request to yourself.");
        }

        // Validate that both users exist
        if (!userRepository.existsById(senderId)) {
            throw new IllegalArgumentException("Sender user does not exist: " + senderId);
        }
        if (!userRepository.existsById(receiverId)) {
            throw new IllegalArgumentException("Receiver user does not exist: " + receiverId);
        }

        // 🔍 Check if already exists (pending/accepted/rejected)
        Optional<Friend> existing = friendRepository.findExistingFriend(senderId, receiverId);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Friend request already exists or users are already friends.");
        }

        Friend request = new Friend();
        request.setSenderId(senderId);
        request.setReceiverId(receiverId);
        request.setStatus(Friend.Status.PENDING);

        return friendRepository.save(request);
    }


    // ---------------- UPDATE STATUS ----------------
    public Optional<Friend> updateStatus(Long requestId, Friend.Status status) {
        return friendRepository.findById(requestId).map(req -> {
            req.setStatus(status);
            return friendRepository.save(req);
        });
    }


    // ---------------- GET ACCEPTED FRIENDS ----------------
    public List<Friend> getFriends(Long userId) {
        return friendRepository.findFriends(userId);
    }

    // ---------------- PENDING REQUEST LIST ----------------
    public List<Friend> getPendingRequests(Long userId) {
        return friendRepository.findByReceiverIdAndStatus(userId, Friend.Status.PENDING);
    }

    // ---------------- CHECK IF TWO USERS ARE FRIENDS ----------------
    public boolean areFriends(Long user1, Long user2) {
        return friendRepository.findExistingFriend(user1, user2)
                .map(f -> f.getStatus() == Friend.Status.ACCEPTED)
                .orElse(false);
    }

    // ---------------- DELETE FRIEND ----------------
    public void deleteFriend(Long id) {
        friendRepository.deleteById(id);
    }
}
