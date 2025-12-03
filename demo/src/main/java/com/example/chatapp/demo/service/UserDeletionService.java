package com.example.chatapp.demo.service;

import com.example.chatapp.demo.repository.FriendRepository;
import com.example.chatapp.demo.repository.MessageRepository;
import com.example.chatapp.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UserDeletionService {

    private final FriendRepository friendRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public UserDeletionService(FriendRepository friendRepository,
                               MessageRepository messageRepository,
                               UserRepository userRepository) {
        this.friendRepository = friendRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void deleteUserSafely(Long userId) {

        // Delete messages from user
        messageRepository.deleteByFromUserId(userId);

        // Delete messages to user
        messageRepository.deleteByToUserId(userId);

        // Delete from friends table
        friendRepository.deleteBySenderId(userId);
        friendRepository.deleteByReceiverId(userId);

        // Finally delete the user
        userRepository.deleteById(userId);
    }
}
