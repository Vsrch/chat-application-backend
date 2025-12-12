package com.example.chatapp.demo.restcontroller;

import com.example.chatapp.demo.dto.FriendRequestdto;
import com.example.chatapp.demo.model.Friend;
import com.example.chatapp.demo.model.User;
import com.example.chatapp.demo.service.FriendService;
import com.example.chatapp.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/friends")
public class Friendcontroller {

    private final FriendService friendService;
    private final UserService userService;

    public Friendcontroller(FriendService friendService,
                            UserService userService) {
        this.friendService = friendService;
        this.userService = userService;
    }

    // SEND FRIEND REQUEST
    @PostMapping("/send-request")
    public ResponseEntity<?> sendFriendRequest(@RequestBody FriendRequestdto dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User current = userService.getUserByUsername(username);
        try {
            Friend saved = friendService.sendRequest(current.getId(), dto.getFriendId());
            return ResponseEntity.ok(Map.of("message","Friend request sent", "data", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    // ACCEPT
    @PutMapping("/accept/{id}")
    public ResponseEntity<?> accept(@PathVariable Long id) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.getUserByUsername(username);

        try {
            return friendService.updateStatus(id, Friend.Status.ACCEPTED, currentUser.getId())
                    .map(f -> ResponseEntity.ok(Map.of("message", "Accepted")))
                    .orElse(ResponseEntity.status(404).body(Map.of("error", "Request not found")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PutMapping("/reject/{id}")
    public ResponseEntity<?> reject(@PathVariable Long id) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.getUserByUsername(username);

        try {
            return friendService.updateStatus(id, Friend.Status.REJECTED, currentUser.getId())
                    .map(f -> ResponseEntity.ok(Map.of("message", "Rejected")))
                    .orElse(ResponseEntity.status(404).body(Map.of("error", "Request not found")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }



    @GetMapping("/{userId}")
    public List<Friend> getFriends(@PathVariable Long userId) {
        return friendService.getFriends(userId); // returns accepted friends
    }

    @GetMapping("/pending/{userId}")
    public List<Friend> getPending(@PathVariable Long userId) {
        return friendService.getPendingRequests(userId);
    }


    // DELETE FRIEND — USING JWT AUTHENTICATION
    @DeleteMapping("/remove/{friendId}")
    public ResponseEntity<?> removeFriend(@PathVariable Long friendId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.getUserByUsername(username);
        friendService.removeFriendship(currentUser.getId(), friendId);
        return ResponseEntity.ok(Map.of("message","Friend removed successfully", "removedBy", username, "friendId", friendId));
    }

}
