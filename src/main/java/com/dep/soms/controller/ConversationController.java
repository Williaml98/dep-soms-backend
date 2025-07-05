//package com.dep.soms.controller;
//
//import com.dep.soms.dto.message.ConversationDto;
//import com.dep.soms.dto.message.ConversationRequest;
//import com.dep.soms.service.ConversationService;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/conversations")
//public class ConversationController {
//    @Autowired private ConversationService conversationService;
//
//    @GetMapping
//    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
//    public ResponseEntity<List<ConversationDto>> getConversations() {
//        List<ConversationDto> conversations = conversationService.getConversationsForCurrentUser();
//        return ResponseEntity.ok(conversations);
//    }
//
//    // Add this new endpoint for marking conversations as read
//    @PostMapping("/{id}/mark-read")
//    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
//    public ResponseEntity<?> markConversationAsRead(@PathVariable Long id) {
//        try {
//            conversationService.markConversationAsRead(id);
//            return ResponseEntity.ok().build();
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Failed to mark conversation as read: " + e.getMessage());
//        }
//    }
//
//    @GetMapping("/{id}")
//    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
//    public ResponseEntity<ConversationDto> getConversationById(@PathVariable Long id) {
//        ConversationDto conversation = conversationService.getConversationById(id);
//        return ResponseEntity.ok(conversation);
//    }
//
//    @PostMapping
//    @PreAuthorize("hasRole('CLIENT', 'ADMIN')")
//    public ResponseEntity<ConversationDto> createConversation(@Valid @RequestBody ConversationRequest request) {
//        ConversationDto conversation = conversationService.createConversation(request);
//        return new ResponseEntity<>(conversation, HttpStatus.CREATED);
//    }
//
//    @PutMapping("/{id}/assign/{adminId}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<ConversationDto> assignAdmin(
//            @PathVariable Long id,
//            @PathVariable Long adminId) {
//        ConversationDto conversation = conversationService.assignAdminToConversation(id, adminId);
//        return ResponseEntity.ok(conversation);
//    }
//
//    @PutMapping("/{id}/status")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<ConversationDto> updateStatus(
//            @PathVariable Long id,
//            @RequestParam String status) {
//        ConversationDto conversation = conversationService.updateConversationStatus(id, status);
//        return ResponseEntity.ok(conversation);
//    }
//}

package com.dep.soms.controller;

import com.dep.soms.dto.message.ConversationDto;
import com.dep.soms.dto.message.ConversationRequest;
import com.dep.soms.service.ConversationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<List<ConversationDto>> getConversations() {
        try {
            List<ConversationDto> conversations = conversationService.getConversationsForCurrentUser();
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<ConversationDto> getConversationById(@PathVariable Long id) {
        try {
            ConversationDto conversation = conversationService.getConversationById(id);
            return ResponseEntity.ok(conversation);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // FIXED: Changed from hasRole('CLIENT', 'ADMIN') to hasAnyRole('ADMIN', 'CLIENT')
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<ConversationDto> createConversation(@Valid @RequestBody ConversationRequest request) {
        try {
            ConversationDto conversation = conversationService.createConversation(request);
            return new ResponseEntity<>(conversation, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PutMapping("/{id}/assign/{adminId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConversationDto> assignAdmin(
            @PathVariable Long id,
            @PathVariable Long adminId) {
        try {
            ConversationDto conversation = conversationService.assignAdminToConversation(id, adminId);
            return ResponseEntity.ok(conversation);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConversationDto> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            ConversationDto conversation = conversationService.updateConversationStatus(id, status);
            return ResponseEntity.ok(conversation);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/mark-read")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<?> markConversationAsRead(@PathVariable Long id) {
        try {
            conversationService.markConversationAsRead(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to mark conversation as read: " + e.getMessage());
        }
    }
}
