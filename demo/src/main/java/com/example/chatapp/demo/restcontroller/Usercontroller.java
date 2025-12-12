package com.example.chatapp.demo.restcontroller;

import com.example.chatapp.demo.model.User;
import com.example.chatapp.demo.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class Usercontroller {

    private final UserRepository userRepository;

    public Usercontroller(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(u -> ResponseEntity.ok((Object) u))
                .orElseGet(() ->
                        ResponseEntity.status(404)
                                .body(Map.of("error", "User not found"))
                );
    }

    // ---------------- UPDATE PROFILE ----------------
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) {
        return userRepository.findById(id).map(user -> {

            if (body.containsKey("fullName"))
                user.setFullName(body.get("fullName").toString());

            if (body.containsKey("bio"))
                user.setBio(body.get("bio").toString());

            if (body.containsKey("phone"))
                user.setPhone(body.get("phone").toString());

            if (body.containsKey("avatarUrl"))
                user.setAvatarUrl(body.get("avatarUrl").toString());

            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "message", "User updated successfully",
                    "user", user
            ));

        }).orElse(ResponseEntity.status(404).body(
                Map.of("error", "User not found")
        ));
    }

    // ---------------- DELETE USER ----------------
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {

        return userRepository.findById(id).map(user -> {

            userRepository.delete(user);

            return ResponseEntity.ok(
                    Map.of(
                            "message", "User deleted successfully",
                            "userId", id
                    )
            );

        }).orElse(ResponseEntity.status(404).body(
                Map.of("error", "User not found")
        ));
    }

}