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

    // in FriendService
    public Friend sendRequest(Long senderId, Long receiverId) {
        if (senderId == null || receiverId == null) throw new IllegalArgumentException("userId and friendId required");
        if (senderId.equals(receiverId)) throw new IllegalArgumentException("Cannot send request to yourself");
        if (!userRepository.existsById(senderId)) throw new IllegalArgumentException("Sender not found");
        if (!userRepository.existsById(receiverId)) throw new IllegalArgumentException("Receiver not found");

        Optional<Friend> existing = friendRepository.findExistingFriend(senderId, receiverId);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Friend request exists or already friends");
        }

        Friend f = new Friend();
        f.setSenderId(senderId);
        f.setReceiverId(receiverId);
        f.setStatus(Friend.Status.PENDING);
        return friendRepository.save(f);
    }


    public Optional<Friend> updateStatus(Long requestId, Friend.Status status, Long currentUserId) {
        return friendRepository.findById(requestId).map(req -> {
            if (!req.getReceiverId().equals(currentUserId)) {
                throw new IllegalArgumentException("Only receiver can accept/reject");
            }
            req.setStatus(status);
            return friendRepository.save(req);
        });
    }


    public List<Friend> getFriends(Long userId) {
        return friendRepository.findFriends(userId);
    }

    public List<Friend> getPendingRequests(Long userId) {
        return friendRepository.findByReceiverIdAndStatus(userId, Friend.Status.PENDING);
    }

    // FINAL DELETE LOGIC
    public void removeFriendship(Long userId, Long friendId) {

        Optional<Friend> relation = friendRepository.findExistingFriend(userId, friendId);

        if (relation.isEmpty()) {
            throw new RuntimeException("No friendship found between users.");
        }

        friendRepository.delete(relation.get());
    }
    public boolean areFriends(Long userId, Long friendId) {
        return friendRepository.existsBySenderIdAndReceiverIdAndStatus(userId, friendId, Friend.Status.ACCEPTED)
                || friendRepository.existsBySenderIdAndReceiverIdAndStatus(friendId, userId, Friend.Status.ACCEPTED);
    }


}
