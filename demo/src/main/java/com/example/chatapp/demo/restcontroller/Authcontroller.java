package com.example.chatapp.demo.restcontroller;

import com.example.chatapp.demo.dto.LoginRequestdto;
import com.example.chatapp.demo.dto.RegisterRequestdto;
import com.example.chatapp.demo.dto.AuthenticationResponse;
import com.example.chatapp.demo.model.User;
import com.example.chatapp.demo.repository.UserRepository;
import com.example.chatapp.demo.security.JwtUtil;
import com.example.chatapp.demo.service.Authservice;
import com.example.chatapp.demo.service.CustomUserDetailsService;
import com.example.chatapp.demo.service.UserDeletionService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class Authcontroller {

    @Autowired
    private Authservice authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDeletionService userDeletionService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtUtil jwtUtil;


    // ------------------ REGISTER ------------------
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestdto request) {

        try {
            String username = request.getUsername();
            String email = request.getEmail();
            String password = request.getPassword();

            String fullName = request.getFullName();
            String bio = request.getBio();
            String phone = request.getPhone();
            String avatarUrl = request.getAvatarUrl();

            // Validation
            if (username == null || username.isBlank())
                return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));

            if (email == null || email.isBlank())
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));

            if (password == null || password.isBlank())
                return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));

            if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"))
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid email format"));

            if (password.length() < 6)
                return ResponseEntity.badRequest().body(Map.of("error", "Weak password"));

            if (userRepository.existsByEmail(email))
                return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));

            if (userRepository.existsByUsername(username))
                return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));

            // DO NOT ENCODE PASSWORD HERE (Authservice does it)
            User saved = authService.register(
                    username,
                    email,
                    password,   // raw password
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
            e.printStackTrace();
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }


    // ------------------ LOGIN (JWT) - username OR email ------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestdto loginRequest) {

        // Pick username if provided, else fall back to email
        String loginId = loginRequest.getUsername();
        if (loginId == null || loginId.isBlank()) {
            loginId = loginRequest.getEmail();
        }

        if (loginId == null || loginId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username or Email is required"));
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginId,
                            loginRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid username/email or password"));
        }

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(loginId);
        String token = jwtUtil.generateToken(userDetails.getUsername());

        return ResponseEntity.ok(new AuthenticationResponse(token));
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
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(
                    Map.of("error", "Failed to delete user: " + ex.getMessage())
            );
        }
    }
}
