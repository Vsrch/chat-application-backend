package com.example.chatapp.demo.service;

import com.example.chatapp.demo.model.User;
import com.example.chatapp.demo.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class Authservice {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    public Authservice(UserRepository userRepository, BCryptPasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    public User register(String username, String email, String password,
                         String fullName, String bio, String phone, String avatarUrl) {

        // 🔍 Check if email already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        // 🔐 Encrypt password
        String encryptedPassword = encoder.encode(password);

        // 🔹 Create user object
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encryptedPassword);
        user.setFullName(fullName);
        user.setBio(bio);
        user.setPhone(phone);
        user.setAvatarUrl(avatarUrl);

        return userRepository.save(user);
    }

    public boolean login(String username, String rawPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        return encoder.matches(rawPassword, user.getPassword());
    }

    public boolean loginFlexible(String input, String rawPassword) {

        Optional<User> userOpt;

        if (input.contains("@")) {
            userOpt = userRepository.findByEmail(input);
        } else {
            userOpt = userRepository.findByUsername(input);
        }

        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        return encoder.matches(rawPassword, user.getPassword());
    }
}
