package com.dep.soms.controller;

import com.dep.soms.dto.message.MessageDto;
import com.dep.soms.dto.message.MessageRequest;
import com.dep.soms.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversations/{conversationId}/messages")
public class MessageController {
    @Autowired
    private MessageService messageService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<List<MessageDto>> getMessages(@PathVariable Long conversationId) {
        List<MessageDto> messages = messageService.getMessagesByConversationId(conversationId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<MessageDto> sendMessage(
            @PathVariable Long conversationId,
            @Valid @RequestBody MessageRequest request) {
        MessageDto message = messageService.sendMessage(conversationId, request);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<Map<String, Long>> getUnreadMessageCount() {
        long count = messageService.getUnreadMessageCount();
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @DeleteMapping("/{messageId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long conversationId,
            @PathVariable Long messageId) {
        messageService.deleteMessage(conversationId, messageId);
        return ResponseEntity.noContent().build();
    }


}
