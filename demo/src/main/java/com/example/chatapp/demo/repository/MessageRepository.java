package com.example.chatapp.demo.repository;

import com.example.chatapp.demo.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message,Long> {
//    List<Message> findByFromUserIdAndToUserIdOrderByTimestampAsc(Long f, Long t);
//    List<Message> findByToUserIdOrderByTimestampAsc(Long t);
}

