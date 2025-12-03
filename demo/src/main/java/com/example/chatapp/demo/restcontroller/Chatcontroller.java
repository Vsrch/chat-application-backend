package com.example.chatapp.demo.restcontroller;

import com.example.chatapp.demo.model.Message;
import com.example.chatapp.demo.service.Chatservice;
import com.example.chatapp.demo.service.FriendService;
import com.example.chatapp.demo.repository.UserRepository;
import org.springframework.http.ResponseEntity;
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

        // --- Validate fields exist ---
        if (!body.containsKey("fromUserId") || !body.containsKey("toUserId") || !body.containsKey("content")) {
            return ResponseEntity.badRequest().body(Map.of("error", "fromUserId, toUserId, and content are required"));
        }

        Long fromUserId;
        Long toUserId;

        try {
            fromUserId = Long.parseLong(body.get("fromUserId").toString());
            toUserId = Long.parseLong(body.get("toUserId").toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "User IDs must be numbers"));
        }

        String content = body.get("content").toString().trim();

        // --- Validate user existence ---
        if (!userRepository.existsById(fromUserId) || !userRepository.existsById(toUserId)) {
            return ResponseEntity.status(404).body(Map.of("error", "Sender or receiver does not exist"));
        }

        // --- Validate sender != receiver ---
        if (fromUserId.equals(toUserId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot send a message to yourself"));
        }

        // --- Validate message not empty ---
        if (content.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message cannot be empty"));
        }

        // --- Validate max message length ---
        if (content.length() > 500) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message too long (max 500 characters allowed)"));
        }

        // --- Validate friendship ---
        if (!friendService.areFriends(fromUserId, toUserId)) {
            return ResponseEntity.status(403).body(Map.of("error", "You can send messages only to accepted friends"));
        }

        // --- SAVE MESSAGE ---
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
