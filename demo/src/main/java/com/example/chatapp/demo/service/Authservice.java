package com.example.chatapp.demo.service;

import com.example.chatapp.demo.model.User;
import com.example.chatapp.demo.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class Authservice {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public Authservice(UserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    public User register(String username, String email, String password,
                         String fullName, String bio, String phone, String avatarUrl) {

        // === VALIDATIONS (centralized here) ===

        if (username == null || username.isBlank()) {
            throw new RuntimeException("Username is required");
        }

        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (!email.contains("@") || !email.contains(".")) {
            throw new RuntimeException("Invalid email format");
        }

        if (password == null || password.isBlank()) {
            throw new RuntimeException("Password is required");
        }

        if (password.length() < 6) {
            throw new RuntimeException("Weak password");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        // Encrypt password
        String encryptedPassword = encoder.encode(password);

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
