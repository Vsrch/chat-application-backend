package com.example.chatapp.demo.service;

import com.example.chatapp.demo.model.User;
import com.example.chatapp.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class Authservice {

    private final UserRepository userRepository;

    public Authservice(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Register a new user
    public User registerUser(String username, String password) {
        // Check if username already exists
        Optional<User> existing = userRepository.findAll().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();

        if (existing.isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        return userRepository.save(user);
    }

    // Validate login
    public boolean validateUser(String username, String password) {
        List<User> users = userRepository.findAll();
        return users.stream()
                .anyMatch(u -> u.getUsername().equals(username)
                        && u.getPassword().equals(password));
    }

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get user by ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
}
