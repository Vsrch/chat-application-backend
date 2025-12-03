package com.example.chatapp.demo.restcontroller;

import com.example.chatapp.demo.model.User;
import com.example.chatapp.demo.repository.UserRepository;
import com.example.chatapp.demo.service.Authservice;
import com.example.chatapp.demo.service.UserDeletionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class Authcontroller {

    private final Authservice authService;
    private final UserRepository userRepository;
    private final UserDeletionService userDeletionService;

    public Authcontroller(Authservice authService, UserRepository userRepository,UserDeletionService userDeletionService) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.userDeletionService = userDeletionService;
    }

    // ------------------ REGISTER ------------------
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {
        try {
            String username = (String) body.get("username");
            String email = (String) body.get("email");
            String password = (String) body.get("password");

            // New profile fields
            String fullName = (String) body.get("fullName");
            String bio = (String) body.get("bio");
            String phone = (String) body.get("phone");
            String avatarUrl = (String) body.get("avatarUrl");

            // ------------------ VALIDATIONS ------------------

// Missing fields
            if (username == null || username.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
            }
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }
            if (password == null || password.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
            }

// Invalid email format
            if (!email.contains("@") || !email.contains(".")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid email format"));
            }

// Weak password
            if (password.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("error", "Weak password"));
            }

// Duplicate email
            if (userRepository.existsByEmail(email)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
            }

// Duplicate username
            if (userRepository.existsByUsername(username)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
            }

            User saved = authService.register(
                    username,
                    email,
                    password,
                    fullName,
                    bio,
                    phone,
                    avatarUrl
            );

            return ResponseEntity.ok(
                    Map.of(
                            "message", "User registered successfully",
                            "user", saved
                    )
            );

        } catch (Exception e) {
            e.printStackTrace(); // <-- IMPORTANT for debugging
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }


    }


    // ------------------ LOGIN ------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> body) {

        String email = body.get("email") != null ? body.get("email").toString() : null;
        String username = body.get("username") != null ? body.get("username").toString() : null;
        String password = body.get("password") != null ? body.get("password").toString() : null;

        // Missing fields
        if ((email == null && username == null)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email or Username is required"));
        }
        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password required"));
        }

        // Flexible login
        String input = email != null ? email : username;

        boolean success = authService.loginFlexible(input, password);

        if (!success) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        return ResponseEntity.ok(Map.of("message", "Login successful"));
    }


    // ------------------ GET ALL USERS ------------------
    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ------------------ DELETE USER ------------------
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {

        if (!userRepository.existsById(id)) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        try {
            userDeletionService.deleteUserSafely(id);
            return ResponseEntity.ok(Map.of(
                    "message", "User deleted successfully",
                    "userId", id
            ));
        }
        catch (Exception ex) {
            return ResponseEntity.status(500).body(
                    Map.of("error", "Failed to delete user: " + ex.getMessage())
            );
        }
    }

}

