package com.example.chatapp.demo.restcontroller;

import com.example.chatapp.demo.model.Message;
import com.example.chatapp.demo.service.Chatservice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class Chatcontroller {

    private final Chatservice chatService;

    public Chatcontroller(Chatservice chatService) {
        this.chatService = chatService;
    }

    // Send a new message
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> body) {
        Long fromUserId = Long.valueOf(body.get("fromUserId").toString());
        Long toUserId = Long.valueOf(body.get("toUserId").toString());
        String content = body.get("content").toString();

        Message message = chatService.send(new Message(fromUserId, toUserId, content));
        return ResponseEntity.ok(Map.of(
                "message", "Message sent successfully",
                "data", message
        ));
    }

    // Get all messages
    @GetMapping("/messages")
    public ResponseEntity<List<Message>> getAllMessages() {
        return ResponseEntity.ok(chatService.getAllMessages());
    }

    // Get inbox (messages received by specific user)
    @GetMapping("/messages/{toUserId}")
    public ResponseEntity<List<Message>> getMessagesForUser(@PathVariable Long toUserId) {
        return ResponseEntity.ok(chatService.inbox(toUserId));
    }
    @GetMapping("/messages/{fromUserId}/{toUserId}")
    public ResponseEntity<?> getMessagesBetweenUsers(
            @PathVariable Long fromUserId,
            @PathVariable Long toUserId) {
        return ResponseEntity.ok(chatService.getMessagesBetween(fromUserId, toUserId));
    }

    // Delete a message by ID
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long id) {
        chatService.deleteMessage(id);
        return ResponseEntity.ok(Map.of("message", "Message deleted successfully"));
    }

}
