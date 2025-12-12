package com.example.chatapp.demo.restcontroller;

import com.example.chatapp.demo.model.Message;
import com.example.chatapp.demo.service.Chatservice;
import com.example.chatapp.demo.service.FriendService;
import com.example.chatapp.demo.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/chat")
public class Chatcontroller {

    private final Chatservice chatService;
    private final FriendService friendService;
    private final UserRepository userRepository;

    public Chatcontroller(Chatservice chatService,
                          FriendService friendService,
                          UserRepository userRepository) {
        this.chatService = chatService;
        this.friendService = friendService;
        this.userRepository = userRepository;
    }

    // ---------------------- SEND MESSAGE ----------------------
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> body) {

        // 1️⃣ Get logged-in username from JWT
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Load sender user
        var sender = userRepository.findByUsername(username)
                .orElse(null);

        if (sender == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid JWT user"));
        }

        Long fromUserId = sender.getId();

        // 2️⃣ Validate receiverId
        if (!body.containsKey("toUserId") || !body.containsKey("content")) {
            return ResponseEntity.badRequest().body(Map.of("error", "toUserId and content are required"));
        }

        Long toUserId;

        try {
            toUserId = Long.parseLong(body.get("toUserId").toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "toUserId must be a number"));
        }

        // 3️⃣ Validate content
        String content = body.get("content").toString().trim();
        if (content.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message cannot be empty"));
        }
        if (content.length() > 500) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message too long"));
        }

        // 4️⃣ Validate users exist
        if (!userRepository.existsById(toUserId)) {
            return ResponseEntity.status(404).body(Map.of("error", "Receiver does not exist"));
        }

        if (fromUserId.equals(toUserId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot message yourself"));
        }

        // 5️⃣ Validate friendship
        if (!friendService.areFriends(fromUserId, toUserId)) {
            return ResponseEntity.status(403).body(Map.of("error", "You can send messages only to accepted friends"));
        }

        // 6️⃣ SAVE MESSAGE
        Message msg = chatService.send(new Message(fromUserId, toUserId, content));

        return ResponseEntity.ok(Map.of(
                "message", "Message sent successfully",
                "data", msg
        ));
    }


    // ---------------------- INBOX ----------------------
    @GetMapping("/messages/{userId}")
    public ResponseEntity<?> inbox(@PathVariable Long userId) {

        if (!userRepository.existsById(userId)) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        return ResponseEntity.ok(Map.of("messages", chatService.inbox(userId)));
    }

    // ---------------------- CONVERSATION ----------------------
    @GetMapping("/conversation/{from}/{to}")
    public ResponseEntity<?> conversation(@PathVariable Long from, @PathVariable Long to) {

        if (!userRepository.existsById(from) || !userRepository.existsById(to)) {
            return ResponseEntity.status(404).body(Map.of("error", "One or both users not found"));
        }

        if (!friendService.areFriends(from, to)) {
            return ResponseEntity.status(403).body(Map.of("error", "Users are not friends"));
        }

        return ResponseEntity.ok(
                Map.of("conversation", chatService.getMessagesBetween(from, to))
        );
    }

    // ---------------------- MARK MESSAGE READ ----------------------
    @PutMapping("/read/{id}")
    public ResponseEntity<?> markRead(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(
                    Map.of("message", "Message marked as read", "data", chatService.markAsRead(id))
            );
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }
    // ---------------------- DELETE MESSAGE ----------------------
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long id) {
        try {
            chatService.deleteMessage(id);
            return ResponseEntity.ok(Map.of("message", "Message deleted successfully", "id", id));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

}
