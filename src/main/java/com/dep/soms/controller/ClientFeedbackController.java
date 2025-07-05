package com.dep.soms.controller;
import com.dep.soms.dto.client.ClientFeedbackDto;
import com.dep.soms.dto.client.ClientFeedbackRequest;
import com.dep.soms.service.ClientFeedbackService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/feedback")
public class ClientFeedbackController {

    @Autowired
    private ClientFeedbackService clientFeedbackService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<ClientFeedbackDto>> getAllFeedback() {
        List<ClientFeedbackDto> feedbackList = clientFeedbackService.getAllFeedback();
        return ResponseEntity.ok(feedbackList);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLIENT')")
    public ResponseEntity<ClientFeedbackDto> getFeedbackById(@PathVariable Long id) {
        ClientFeedbackDto feedback = clientFeedbackService.getFeedbackById(id);
        return ResponseEntity.ok(feedback);
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLIENT')")
    public ResponseEntity<List<ClientFeedbackDto>> getFeedbackByClientId(@PathVariable Long clientId) {
        List<ClientFeedbackDto> feedbackList = clientFeedbackService.getFeedbackByClientId(clientId);
        return ResponseEntity.ok(feedbackList);
    }

    @GetMapping("/site/{siteId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<ClientFeedbackDto>> getFeedbackBySiteId(@PathVariable Long siteId) {
        List<ClientFeedbackDto> feedbackList = clientFeedbackService.getFeedbackBySiteId(siteId);
        return ResponseEntity.ok(feedbackList);
    }

    @GetMapping("/guard/{guardId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<ClientFeedbackDto>> getFeedbackByGuardId(@PathVariable Long guardId) {
        List<ClientFeedbackDto> feedbackList = clientFeedbackService.getFeedbackByGuardId(guardId);
        return ResponseEntity.ok(feedbackList);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLIENT')")
    public ResponseEntity<ClientFeedbackDto> createFeedback(@Valid @RequestBody ClientFeedbackRequest request) {
        ClientFeedbackDto createdFeedback = clientFeedbackService.createFeedback(request);
        return ResponseEntity.ok(createdFeedback);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ClientFeedbackDto> updateFeedback(
            @PathVariable Long id,
            @Valid @RequestBody ClientFeedbackRequest request) {
        ClientFeedbackDto updatedFeedback = clientFeedbackService.updateFeedback(id, request);
        return ResponseEntity.ok(updatedFeedback);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteFeedback(@PathVariable Long id) {
        clientFeedbackService.deleteFeedback(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/client/{clientId}/average")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLIENT')")
    public ResponseEntity<Map<String, Double>> getAverageRatingByClientId(@PathVariable Long clientId) {
        Double averageRating = clientFeedbackService.getAverageRatingByClientId(clientId);
        return ResponseEntity.ok(Map.of("averageRating", averageRating != null ? averageRating : 0.0));
    }

    @GetMapping("/site/{siteId}/average")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Map<String, Double>> getAverageRatingBySiteId(@PathVariable Long siteId) {
        Double averageRating = clientFeedbackService.getAverageRatingBySiteId(siteId);
        return ResponseEntity.ok(Map.of("averageRating", averageRating != null ? averageRating : 0.0));
    }

    @GetMapping("/guard/{guardId}/average")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Map<String, Double>> getAverageGuardRatingByGuardId(@PathVariable Long guardId) {
        Double averageRating = clientFeedbackService.getAverageGuardRatingByGuardId(guardId);
        return ResponseEntity.ok(Map.of("averageRating", averageRating != null ? averageRating : 0.0));
    }
}
