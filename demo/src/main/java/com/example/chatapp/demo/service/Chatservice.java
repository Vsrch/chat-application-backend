package com.example.chatapp.demo.service;

import com.example.chatapp.demo.model.Message;
import com.example.chatapp.demo.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class Chatservice {

    private final MessageRepository messageRepository;
    private final FriendService friendService;
    private final MessageCryptoService cryptoService;

    public Chatservice(MessageRepository messageRepository,
                       FriendService friendService,
                       MessageCryptoService cryptoService) {
        this.messageRepository = messageRepository;
        this.friendService = friendService;
        this.cryptoService = cryptoService;
    }

    // -------------------- SEND MESSAGE --------------------
    public Message send(Message message) {

        // Validate content BEFORE encryption
        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            throw new RuntimeException("Message cannot be empty");
        }

        if (message.getContent().length() > 500) {
            throw new RuntimeException("Message too long (max 500 characters)");
        }

        // Validate friendship
        if (!friendService.areFriends(message.getFromUserId(), message.getToUserId())) {
            throw new RuntimeException("You can only send messages to accepted friends");
        }

        // Encrypt message
        String encryptedContent = cryptoService.encrypt(message.getContent());
        message.setContent(encryptedContent);

        message.setSentAt(LocalDateTime.now());
        return messageRepository.save(message);
    }


    // -------------------- USER INBOX --------------------
    public List<Message> inbox(Long userId) {
        List<Message> messages = messageRepository.findByToUserId(userId);

        // Mark undelivered messages
        messages.stream()
                .filter(m -> m.getDeliveredAt() == null)
                .forEach(m -> m.setDeliveredAt(LocalDateTime.now()));

        messageRepository.saveAll(messages);

        // Decrypt before returning
        messages.forEach(m -> m.setContent(cryptoService.decrypt(m.getContent())));
        return messages;
    }

    // -------------------- GET CHAT BETWEEN TWO USERS --------------------
    public List<Message> getMessagesBetween(Long from, Long to) {
        List<Message> messages = messageRepository.findConversation(from, to);

        // Mark unread messages
        messages.stream()
                .filter(m -> m.getReadAt() == null && m.getToUserId().equals(to))
                .forEach(m -> m.setReadAt(LocalDateTime.now()));

        messageRepository.saveAll(messages);

        // Decrypt before returning
        messages.forEach(m -> m.setContent(cryptoService.decrypt(m.getContent())));

        return messages;
    }

    // -------------------- MARK MESSAGE READ --------------------
    public Message markAsRead(Long messageId) {
        Message m = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message Not Found"));

        m.setReadAt(LocalDateTime.now());
        Message saved = messageRepository.save(m);

        // Decrypt before returning
        saved.setContent(cryptoService.decrypt(saved.getContent()));
        return saved;
    }
    // -------------------- DELETE MESSAGE --------------------
    public void deleteMessage(Long messageId) {
        Message msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        messageRepository.delete(msg);
    }

}
