package com.example.chatapp.demo.service;

import com.example.chatapp.demo.model.User;
import com.example.chatapp.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.Map;



@Service
public class UserProfileService {

    private final UserRepository userRepository;

    public UserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User updateUser(Long id, Map<String, Object> updates) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ===== VALIDATION =====

        // username
        if (updates.containsKey("username")) {
            String username = updates.get("username").toString();
            if (username.length() < 3)
                throw new RuntimeException("Username must be at least 3 characters");

            if (userRepository.existsByUsername(username) && !user.getUsername().equals(username))
                throw new RuntimeException("Username already taken");

            user.setUsername(username);
        }

        // fullName
        if (updates.containsKey("fullName")) {
            user.setFullName(updates.get("fullName").toString());
        }

        // bio
        if (updates.containsKey("bio")) {
            user.setBio(updates.get("bio").toString());
        }

        // phone
        if (updates.containsKey("phone")) {
            String phone = updates.get("phone").toString();
            if (!phone.matches("\\d{10}"))
                throw new RuntimeException("Phone must be 10 digits");
            user.setPhone(phone);
        }

        // avatarUrl
        if (updates.containsKey("avatarUrl")) {
            String url = updates.get("avatarUrl").toString();
            if (!url.startsWith("http"))
                throw new RuntimeException("Invalid avatar URL");
            user.setAvatarUrl(url);
        }

        return userRepository.save(user);
    }
}
