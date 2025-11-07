package com.example.chatapp.demo.restcontroller;

import com.example.chatapp.demo.model.User;
import com.example.chatapp.demo.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class Authcontroller {

    private final UserRepository userRepository;

    public Authcontroller(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {
        try {
            System.out.println("📥 Incoming register request: " + body);

            if (body == null || !body.containsKey("username") || !body.containsKey("password")) {
                System.out.println(" Missing fields in request body: " + body);
                return ResponseEntity.badRequest().body(Map.of("error", "Missing username or password"));
            }

            String username = body.get("username") != null ? body.get("username").toString().trim() : "";
            String password = body.get("password") != null ? body.get("password").toString().trim() : "";

            System.out.println(" Parsed username=" + username + ", password=" + password);

            if (username.isEmpty() || password.isEmpty()) {
                System.out.println(" Username or password is empty!");
                return ResponseEntity.badRequest().body(Map.of("error", "Username or password cannot be empty"));
            }

            User user = new User(username, password);
            userRepository.save(user);

            System.out.println(" User registered successfully!");
            return ResponseEntity.ok(Map.of("message", "User registered successfully"));

        } catch (Exception e) {
            System.out.println(" Exception occurred in register: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> body) {
        try {
            String username = body.get("username").toString();
            String password = body.get("password").toString();

            return userRepository.findByUsername(username)
                    .map(u -> {
                        if (u.getPassword().equals(password)) {
                            return ResponseEntity.ok(Map.of("message", "Login successful"));
                        } else {
                            return ResponseEntity.status(401).body(Map.of("error", "Invalid password"));
                        }
                    })
                    .orElse(ResponseEntity.status(404).body(Map.of("error", "User not found")));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
