package com.example.chatapp.demo.repository;

import com.example.chatapp.demo.model.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    List<Friend> findByReceiverIdAndStatus(Long receiverId, Friend.Status status);

    // 🔹 Get bi-directional friend list
    @Query("SELECT f FROM Friend f WHERE (f.senderId = :userId OR f.receiverId = :userId) AND f.status = 'ACCEPTED'")
    List<Friend> findFriends(@Param("userId") Long userId);

    // 🔹 Prevent duplicate friend requests
    @Query("SELECT f FROM Friend f WHERE (f.senderId = :senderId AND f.receiverId = :receiverId) OR (f.senderId = :receiverId AND f.receiverId = :senderId)")
    Optional<Friend> findExistingFriend(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    void deleteBySenderIdOrReceiverId(Long senderId, Long receiverId);
    void deleteBySenderId(Long senderId);
    void deleteByReceiverId(Long receiverId);


}
