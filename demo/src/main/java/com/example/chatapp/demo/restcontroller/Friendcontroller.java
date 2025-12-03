package com.example.chatapp.demo.restcontroller;

import com.example.chatapp.demo.dto.FriendRequestdto;
import com.example.chatapp.demo.service.FriendService;
import com.example.chatapp.demo.model.Friend;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/friends")
public class Friendcontroller {

    private final FriendService friendService;

    public Friendcontroller(FriendService friendService) {
        this.friendService = friendService;
    }

    // ------------------ SEND FRIEND REQUEST ------------------
    @PostMapping("/send-request")
    public ResponseEntity<?> sendFriendRequest(@Valid @RequestBody FriendRequestdto request) {

        Long senderId = request.getUserId();
        Long receiverId = request.getFriendId();

        try {
            Friend saved = friendService.sendRequest(senderId, receiverId);
            return ResponseEntity.ok(
                    Map.of(
                            "message", "Friend request sent",
                            "request", saved
                    )
            );
        } catch (IllegalArgumentException e) {
            // business validation errors (self request, duplicate, user not found)
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Internal error: " + e.getMessage()));
        }
    }

    // ------------------ ACCEPT REQUEST ------------------
    @PutMapping("/accept/{requestId}")
    public ResponseEntity<?> acceptRequest(@PathVariable Long requestId) {
        return friendService.updateStatus(requestId, Friend.Status.ACCEPTED)
                .map(req -> ResponseEntity.ok(Map.of("message", "Friend request accepted", "requestId", requestId)))
                .orElse(ResponseEntity.status(404).body(Map.of("error", "Friend request not found")));
    }

    // ------------------ REJECT REQUEST ------------------
    @PutMapping("/reject/{requestId}")
    public ResponseEntity<?> rejectRequest(@PathVariable Long requestId) {
        return friendService.updateStatus(requestId, Friend.Status.REJECTED)
                .map(req -> ResponseEntity.ok(Map.of("message", "Friend request rejected", "requestId", requestId)))
                .orElse(ResponseEntity.status(404).body(Map.of("error", "Friend request not found")));
    }

    // ------------------ GET FRIEND LIST ------------------
    @GetMapping("/{userId}")
    public List<Friend> getFriends(@PathVariable Long userId) {
        return friendService.getFriends(userId);
    }

    // ------------------ GET PENDING REQUESTS ------------------
    @GetMapping("/pending/{userId}")
    public List<Friend> getPendingRequests(@PathVariable Long userId) {
        return friendService.getPendingRequests(userId);
    }

    // ------------------ DELETE FRIEND ------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFriend(@PathVariable Long id) {
        friendService.deleteFriend(id);
        return ResponseEntity.ok(Map.of("message", "Friend deleted successfully"));
    }
}
