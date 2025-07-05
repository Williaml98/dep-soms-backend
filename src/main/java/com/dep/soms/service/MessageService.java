package com.dep.soms.service;


import com.dep.soms.dto.message.MessageDto;
import com.dep.soms.dto.message.MessageRequest;
import com.dep.soms.exception.ResourceNotFoundException;
import com.dep.soms.model.Conversation;
import com.dep.soms.model.Message;
import com.dep.soms.model.User;
import com.dep.soms.repository.ConversationRepository;
import com.dep.soms.repository.MessageRepository;
import com.dep.soms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private UserRepository userRepository;

    public List<MessageDto> getMessagesByConversationId(Long conversationId) {
        User currentUser = getCurrentUser();
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));

        // Check if user has access to this conversation
        if (!hasRole("ROLE_ADMIN") && !conversation.getClient().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to access messages in this conversation");
        }

        List<Message> messages = messageRepository.findByConversationOrderBySentAtAsc(conversation);

        // Mark messages as read if they weren't sent by the current user
        markMessagesAsRead(messages, currentUser);

        return messages.stream()
                .map(message -> mapToDto(message, currentUser))
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageDto sendMessage(Long conversationId, MessageRequest request) {
        User currentUser = getCurrentUser();
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));

        // Check if user has access to this conversation
        boolean isAdmin = hasRole("ROLE_ADMIN");
        boolean isClient = conversation.getClient().getId().equals(currentUser.getId());

        if (!isAdmin && !isClient) {
            throw new AccessDeniedException("You don't have permission to send messages in this conversation");
        }

        // Check if conversation is closed
        if (conversation.getStatus().equals("CLOSED")) {
            throw new IllegalStateException("Cannot send messages in a closed conversation");
        }

        // Create and save the message
        Message message = Message.builder()
                .content(request.getContent())
                .sender(currentUser)
                .conversation(conversation)
                .build();

        Message savedMessage = messageRepository.save(message);

        // Update the conversation's updated_at timestamp
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        return mapToDto(savedMessage, currentUser);
    }

    @Transactional
    public long getUnreadMessageCount() {
        User currentUser = getCurrentUser();
        return messageRepository.countAllUnreadMessagesNotSentByUser(currentUser);
    }

    @Transactional
    private void markMessagesAsRead(List<Message> messages, User currentUser) {
        messages.stream()
                .filter(message -> !message.getSender().getId().equals(currentUser.getId()) && message.getReadAt() == null)
                .forEach(message -> {
                    message.setReadAt(LocalDateTime.now());
                    messageRepository.save(message);
                });
    }

    private MessageDto mapToDto(Message message, User currentUser) {
        return MessageDto.builder()
                .id(message.getId())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .readAt(message.getReadAt())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFirstName() + " " + message.getSender().getLastName())
                .conversationId(message.getConversation().getId())
                .isFromCurrentUser(message.getSender().getId().equals(currentUser.getId()))
                .build();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
    }

    private boolean hasRole(String roleName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(roleName));
    }

    @Transactional
    public void deleteMessage(Long conversationId, Long messageId) {
        User currentUser = getCurrentUser();
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));

        if (!message.getConversation().getId().equals(conversationId)) {
            throw new IllegalArgumentException("Message does not belong to the specified conversation");
        }

        // Only sender or admin can delete
        boolean isAdmin = hasRole("ROLE_ADMIN");
        boolean isSender = message.getSender().getId().equals(currentUser.getId());

        if (!isAdmin && !isSender) {
            throw new AccessDeniedException("You don't have permission to delete this message");
        }

        messageRepository.delete(message);
    }


}
