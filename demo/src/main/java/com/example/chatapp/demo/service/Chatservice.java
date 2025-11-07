package com.example.chatapp.demo.service;

import com.example.chatapp.demo.model.Message;
import com.example.chatapp.demo.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Chatservice {

    private final MessageRepository messageRepository;

    public Chatservice(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    // Send a new message
    public Message send(Message message) {
        return messageRepository.save(message);
    }

    // Get all messages
    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    // Get messages received by a particular user
    public List<Message> inbox(Long toUserId) {
        return messageRepository.findAll().stream()
                .filter(msg -> msg.getToUserId().equals(toUserId))
                .toList();
    }

    // Delete a message
    public void deleteMessage(Long id) {
        messageRepository.deleteById(id);
    }
    public List<Message> getMessagesBetween(Long fromUserId, Long toUserId) {
        return messageRepository.findAll().stream()
                .filter(msg ->
                        (msg.getFromUserId().equals(fromUserId) && msg.getToUserId().equals(toUserId)) ||
                                (msg.getFromUserId().equals(toUserId) && msg.getToUserId().equals(fromUserId))
                )
                .toList();
    }
}
