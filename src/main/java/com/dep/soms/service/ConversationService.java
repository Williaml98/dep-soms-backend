//package com.dep.soms.service;
//
//import com.dep.soms.dto.message.ConversationDto;
//import com.dep.soms.dto.message.ConversationRequest;
//import com.dep.soms.dto.message.MessageDto;
//import com.dep.soms.exception.ResourceNotFoundException;
//import com.dep.soms.model.Conversation;
//import com.dep.soms.model.Message;
//import com.dep.soms.model.User;
//import com.dep.soms.repository.ConversationRepository;
//import com.dep.soms.repository.MessageRepository;
//import com.dep.soms.repository.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.Collections;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service public class ConversationService {
//
//    private final ConversationRepository conversationRepository;
//    private final MessageRepository messageRepository;
//    private final UserRepository userRepository;
//
//    @Autowired
//    public ConversationService(
//            ConversationRepository conversationRepository,
//            MessageRepository messageRepository,
//            UserRepository userRepository) {
//        this.conversationRepository = conversationRepository;
//        this.messageRepository = messageRepository;
//        this.userRepository = userRepository;
//    }
//
//    /**
//     * Get all conversations for the current user
//     * Admins can see all conversations, clients can only see their own
//     */
//    public List<ConversationDto> getConversationsForCurrentUser() {
//        User currentUser = getCurrentUser();
//        List<Conversation> conversations;
//
//        if (hasRole("ROLE_ADMIN")) {
//            conversations = conversationRepository.findAllByOrderByUpdatedAtDesc();
//        } else {
//            conversations = conversationRepository.findByClientOrderByUpdatedAtDesc(currentUser);
//        }
//
//        return conversations.stream()
//                .map(conversation -> convertToDto(conversation, currentUser))
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * Get a specific conversation by ID
//     * Checks if the current user has access to this conversation
//     */
//    public ConversationDto getConversationById(Long id) {
//        User currentUser = getCurrentUser();
//        Conversation conversation = findConversationById(id);
//
//        // Check if user has access to this conversation
//        if (!hasRole("ROLE_ADMIN") && !conversation.getClient().getId().equals(currentUser.getId())) {
//            throw new AccessDeniedException("You don't have permission to access this conversation");
//        }
//
//        return convertToDto(conversation, currentUser);
//    }
//
//    /**
//     * Create a new conversation with an initial message
//     * Only clients can create conversations
//     */
////    @Transactional
////    public ConversationDto createConversation(ConversationRequest request) {
////        User currentUser = getCurrentUser();
////
////        // Only clients can create conversations
////        if (!hasRole("ROLE_CLIENT")) {
////            throw new AccessDeniedException("Only clients can create new conversations");
////        }
////
////        // Create and save the conversation
////        Conversation conversation = new Conversation();
////        conversation.setSubject(request.getSubject());
////        conversation.setStatus("OPEN");
////        conversation.setClient(currentUser);
////        conversation.setCreatedAt(LocalDateTime.now());
////        conversation.setUpdatedAt(LocalDateTime.now());
////
////        Conversation savedConversation = conversationRepository.save(conversation);
////
////        // Create and save the initial message
////        Message initialMessage = new Message();
////        initialMessage.setContent(request.getInitialMessage());
////        initialMessage.setSender(currentUser);
////        initialMessage.setConversation(savedConversation);
////        initialMessage.setSentAt(LocalDateTime.now());
////
////        messageRepository.save(initialMessage);
////
////        return convertToDto(savedConversation, currentUser);
////    }
//
//    /**
//     * Assign an admin to a conversation
//     * Only admins can assign conversations
//     */
//    @Transactional
//    public ConversationDto assignAdminToConversation(Long conversationId, Long adminId) {
//        User currentUser = getCurrentUser();
//
//        // Only admins can assign conversations
//        if (!hasRole("ROLE_ADMIN")) {
//            throw new AccessDeniedException("Only admins can assign conversations");
//        }
//
//        Conversation conversation = findConversationById(conversationId);
//        User admin = userRepository.findById(adminId)
//                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + adminId));
//
//        // Check if the user is an admin
//        if (!admin.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
//            throw new IllegalArgumentException("The selected user is not an admin");
//        }
//
//        conversation.setAdmin(admin);
//        conversation.setUpdatedAt(LocalDateTime.now());
//        Conversation updatedConversation = conversationRepository.save(conversation);
//
//        return convertToDto(updatedConversation, currentUser);
//    }
//
//    /**
//     * Update the status of a conversation (OPEN or CLOSED)
//     * Only admins can update conversation status
//     */
//    @Transactional
//    public ConversationDto updateConversationStatus(Long conversationId, String status) {
//        User currentUser = getCurrentUser();
//
//        // Only admins can update conversation status
//        if (!hasRole("ROLE_ADMIN")) {
//            throw new AccessDeniedException("Only admins can update conversation status");
//        }
//
//        Conversation conversation = findConversationById(conversationId);
//
//        if (!status.equals("OPEN") && !status.equals("CLOSED")) {
//            throw new IllegalArgumentException("Status must be either OPEN or CLOSED");
//        }
//
//        conversation.setStatus(status);
//        conversation.setUpdatedAt(LocalDateTime.now());
//        Conversation updatedConversation = conversationRepository.save(conversation);
//
//        return convertToDto(updatedConversation, currentUser);
//    }
//
//    /**
//     * Helper method to find a conversation by ID or throw an exception
//     */
//    private Conversation findConversationById(Long id) {
//        return conversationRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + id));
//    }
//
//    /**
//     * Convert a Conversation entity to a ConversationDto
//     */
//    private ConversationDto convertToDto(Conversation conversation, User currentUser) {
//        // Get the last message in the conversation
//        List<Message> messages = messageRepository.findByConversationOrderBySentAtAsc(conversation);
//        MessageDto lastMessageDto = null;
//
//        if (!messages.isEmpty()) {
//            Message lastMessage = messages.get(messages.size() - 1);
//            lastMessageDto = MessageDto.builder()
//                    .id(lastMessage.getId())
//                    .content(lastMessage.getContent())
//                    .sentAt(lastMessage.getSentAt())
//                    .readAt(lastMessage.getReadAt())
//                    .senderId(lastMessage.getSender().getId())
//                    .senderName(lastMessage.getSender().getFirstName() + " " + lastMessage.getSender().getLastName())
//                    .conversationId(lastMessage.getConversation().getId())
//                    .isFromCurrentUser(lastMessage.getSender().getId().equals(currentUser.getId()))
//                    .build();
//        }
//
//        // Count unread messages
//        long unreadCount = messageRepository.countUnreadMessagesByConversationAndNotSender(conversation, currentUser);
//
//        // Build the DTO
//        return ConversationDto.builder()
//                .id(conversation.getId())
//                .subject(conversation.getSubject())
//                .createdAt(conversation.getCreatedAt())
//                .updatedAt(conversation.getUpdatedAt())
//                .status(conversation.getStatus())
//                .clientId(conversation.getClient().getId())
//                .clientName(conversation.getClient().getFirstName() + " " + conversation.getClient().getLastName())
//                .adminId(conversation.getAdmin() != null ? conversation.getAdmin().getId() : null)
//                .adminName(conversation.getAdmin() != null ?
//                        conversation.getAdmin().getFirstName() + " " + conversation.getAdmin().getLastName() : null)
//                .unreadCount((int) unreadCount)
//                .lastMessage(lastMessageDto)
//                .build();
//    }
//
//    /**
//     * Get the current authenticated user
//     */
//    private User getCurrentUser() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String username = authentication.getName();
//        return userRepository.findByUsername(username)
//                .orElseThrow(() -> new IllegalStateException("Current user not found"));
//    }
//
//    /**
//     * Check if the current user has a specific role
//     */
//    private boolean hasRole(String roleName) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        return authentication.getAuthorities().stream()
//                .anyMatch(authority -> authority.getAuthority().equals(roleName));
//    }
//
//    // Add this method to your existing ConversationService class
//
//    @Transactional
//    public void markConversationAsRead(Long conversationId) {
//        // Get current user
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String currentUsername = authentication.getName();
//
//        // Find the conversation
//        Conversation conversation = conversationRepository.findById(conversationId)
//                .orElseThrow(() -> new RuntimeException("Conversation not found"));
//
//        // Mark all messages in this conversation as read by the current user
//        List<Message> unreadMessages = messageRepository.findByConversationIdAndReadAtIsNull(conversationId);
//
//        for (Message message : unreadMessages) {
//            // Only mark as read if the current user is not the sender
//            if (!message.getSender().getUsername().equals(currentUsername)) {
//                message.setReadAt(LocalDateTime.now());
//                messageRepository.save(message);
//            }
//        }
//
//        // Update conversation's updated_at timestamp
//        conversation.setUpdatedAt(LocalDateTime.now());
//        conversationRepository.save(conversation);
//    }
//
//    // Add this method or update your existing createConversation method
//
//    @Transactional
//    public ConversationDto createConversation(ConversationRequest request) {
//        // Get current user
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String currentUsername = authentication.getName();
//        User currentUser = userRepository.findByUsername(currentUsername)
//                .orElseThrow(() -> new RuntimeException("Current user not found"));
//
//        // Create conversation
//        Conversation conversation = new Conversation();
//        conversation.setSubject(request.getSubject());
//        conversation.setStatus("OPEN");
//        conversation.setCreatedAt(LocalDateTime.now());
//        conversation.setUpdatedAt(LocalDateTime.now());
//
//        // Handle different user roles
//        if (currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
//            // Admin is creating conversation with a client
//            User client = userRepository.findById(request.getClientId())
//                    .orElseThrow(() -> new RuntimeException("Client not found"));
//            conversation.setClient(client);
//            conversation.setAssignedAdmin(currentUser);
//        } else {
//            // Client is creating conversation
//            conversation.setClient(currentUser);
//            // Admin can be assigned later
//        }
//
//        conversation = conversationRepository.save(conversation);
//
//        // Create initial message if provided
//        if (request.getInitialMessage() != null && !request.getInitialMessage().trim().isEmpty()) {
//            Message message = new Message();
//            message.setConversation(conversation);
//            message.setSender(currentUser);
//            message.setContent(request.getInitialMessage());
//            message.setSentAt(LocalDateTime.now());
//            messageRepository.save(message);
//        }
//
//        return convertToDto(conversation);
//    }
//
//
//}
//

package com.dep.soms.service;

import com.dep.soms.dto.message.ConversationDto;
import com.dep.soms.dto.message.ConversationRequest;
import com.dep.soms.dto.message.MessageDto;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Autowired
    public ConversationService(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get all conversations for the current user
     * Admins can see all conversations, clients can only see their own
     */
    public List<ConversationDto> getConversationsForCurrentUser() {
        User currentUser = getCurrentUser();
        List<Conversation> conversations;

        if (hasRole("ROLE_ADMIN")) {
            conversations = conversationRepository.findAllByOrderByUpdatedAtDesc();
        } else {
            conversations = conversationRepository.findByClientOrderByUpdatedAtDesc(currentUser);
        }

        return conversations.stream()
                .map(conversation -> convertToDto(conversation, currentUser))
                .collect(Collectors.toList());
    }

    /**
     * Get a specific conversation by ID
     * Checks if the current user has access to this conversation
     */
    public ConversationDto getConversationById(Long id) {
        User currentUser = getCurrentUser();
        Conversation conversation = findConversationById(id);

        // Check if user has access to this conversation
        if (!hasRole("ROLE_ADMIN") && !conversation.getClient().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to access this conversation");
        }

        return convertToDto(conversation, currentUser);
    }

    /**
     * Create a new conversation with an initial message
     * Both admins and clients can create conversations
     */
    @Transactional
    public ConversationDto createConversation(ConversationRequest request) {
        User currentUser = getCurrentUser();

        // Create conversation
        Conversation conversation = new Conversation();
        conversation.setSubject(request.getSubject());
        conversation.setStatus("OPEN");
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());

        // Handle different user roles
        if (hasRole("ROLE_ADMIN")) {
            // Admin is creating conversation with a client
            if (request.getClientId() == null) {
                throw new IllegalArgumentException("Client ID is required when admin creates a conversation");
            }

            User client = userRepository.findById(request.getClientId())
                    .orElseThrow(() -> new RuntimeException("Client not found"));
            conversation.setClient(client);
            conversation.setAdmin(currentUser); // FIXED: Use setAdmin instead of setAssignedAdmin
        } else {
            // Client is creating conversation
            conversation.setClient(currentUser);
            // Admin can be assigned later
        }

        conversation = conversationRepository.save(conversation);

        // Create initial message if provided
        if (request.getInitialMessage() != null && !request.getInitialMessage().trim().isEmpty()) {
            Message message = new Message();
            message.setConversation(conversation);
            message.setSender(currentUser);
            message.setContent(request.getInitialMessage());
            message.setSentAt(LocalDateTime.now());
            messageRepository.save(message);
        }

        return convertToDto(conversation, currentUser); // FIXED: Added currentUser parameter
    }

    /**
     * Assign an admin to a conversation
     * Only admins can assign conversations
     */
    @Transactional
    public ConversationDto assignAdminToConversation(Long conversationId, Long adminId) {
        User currentUser = getCurrentUser();

        // Only admins can assign conversations
        if (!hasRole("ROLE_ADMIN")) {
            throw new AccessDeniedException("Only admins can assign conversations");
        }

        Conversation conversation = findConversationById(conversationId);
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + adminId));

        // Check if the user is an admin
        if (!admin.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            throw new IllegalArgumentException("The selected user is not an admin");
        }

        conversation.setAdmin(admin);
        conversation.setUpdatedAt(LocalDateTime.now());
        Conversation updatedConversation = conversationRepository.save(conversation);

        return convertToDto(updatedConversation, currentUser);
    }

    /**
     * Update the status of a conversation (OPEN or CLOSED)
     * Only admins can update conversation status
     */
    @Transactional
    public ConversationDto updateConversationStatus(Long conversationId, String status) {
        User currentUser = getCurrentUser();

        // Only admins can update conversation status
        if (!hasRole("ROLE_ADMIN")) {
            throw new AccessDeniedException("Only admins can update conversation status");
        }

        Conversation conversation = findConversationById(conversationId);

        if (!status.equals("OPEN") && !status.equals("CLOSED")) {
            throw new IllegalArgumentException("Status must be either OPEN or CLOSED");
        }

        conversation.setStatus(status);
        conversation.setUpdatedAt(LocalDateTime.now());
        Conversation updatedConversation = conversationRepository.save(conversation);

        return convertToDto(updatedConversation, currentUser);
    }

    /**
     * Mark all messages in a conversation as read by the current user
     */
    @Transactional
    public void markConversationAsRead(Long conversationId) {
        User currentUser = getCurrentUser();

        // Find the conversation
        Conversation conversation = findConversationById(conversationId);

        // Check if user has access to this conversation
        if (!hasRole("ROLE_ADMIN") && !conversation.getClient().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to access this conversation");
        }

        // Mark all messages in this conversation as read by the current user
        List<Message> unreadMessages = messageRepository.findByConversationIdAndReadAtIsNull(conversationId);

        for (Message message : unreadMessages) {
            // Only mark as read if the current user is not the sender
            if (!message.getSender().getId().equals(currentUser.getId())) {
                message.setReadAt(LocalDateTime.now());
                messageRepository.save(message);
            }
        }

        // Update conversation's updated_at timestamp
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
    }

    /**
     * Helper method to find a conversation by ID or throw an exception
     */
    private Conversation findConversationById(Long id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + id));
    }

    /**
     * Convert a Conversation entity to a ConversationDto
     */
    private ConversationDto convertToDto(Conversation conversation, User currentUser) {
        // Get the last message in the conversation
        List<Message> messages = messageRepository.findByConversationOrderBySentAtAsc(conversation);
        MessageDto lastMessageDto = null;

        if (!messages.isEmpty()) {
            Message lastMessage = messages.get(messages.size() - 1);
            lastMessageDto = MessageDto.builder()
                    .id(lastMessage.getId())
                    .content(lastMessage.getContent())
                    .sentAt(lastMessage.getSentAt())
                    .readAt(lastMessage.getReadAt())
                    .senderId(lastMessage.getSender().getId())
                    .senderName(lastMessage.getSender().getFirstName() + " " + lastMessage.getSender().getLastName())
                    .conversationId(lastMessage.getConversation().getId())
                    .isFromCurrentUser(lastMessage.getSender().getId().equals(currentUser.getId()))
                    .build();
        }

        // Count unread messages for the current user
        long unreadCount = messageRepository.countUnreadMessagesByConversationAndNotSender(conversation, currentUser);

        // Build the DTO
        return ConversationDto.builder()
                .id(conversation.getId())
                .subject(conversation.getSubject())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .status(conversation.getStatus())
                .clientId(conversation.getClient().getId())
                .clientName(conversation.getClient().getFirstName() + " " + conversation.getClient().getLastName())
                .adminId(conversation.getAdmin() != null ? conversation.getAdmin().getId() : null)
                .adminName(conversation.getAdmin() != null ?
                        conversation.getAdmin().getFirstName() + " " + conversation.getAdmin().getLastName() : null)
                .unreadCount((int) unreadCount)
                .lastMessage(lastMessageDto)
                .build();
    }

    /**
     * Get the current authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
    }

    /**
     * Check if the current user has a specific role
     */
    private boolean hasRole(String roleName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(roleName));
    }
}
