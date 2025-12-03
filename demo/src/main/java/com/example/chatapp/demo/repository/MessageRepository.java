package com.example.chatapp.demo.repository;

import com.example.chatapp.demo.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByToUserId(Long toUserId);

    List<Message> findByFromUserId(Long fromUserId);

    List<Message> findByFromUserIdAndToUserId(Long fromUserId, Long toUserId);
    @Query("SELECT m FROM Message m WHERE (m.fromUserId = :from AND m.toUserId = :to) OR (m.fromUserId = :to AND m.toUserId = :from) ORDER BY m.sentAt ASC")
    List<Message> findConversation(@Param("from") Long from, @Param("to") Long to);
    void deleteByFromUserId(Long id);
    void deleteByToUserId(Long id);


}
