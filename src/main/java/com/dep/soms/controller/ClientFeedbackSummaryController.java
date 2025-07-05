package com.dep.soms.controller;

import com.dep.soms.service.ClientFeedbackSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.dep.soms.dto.client.FeedbackSummaryDto;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/feedback/summary")
public class ClientFeedbackSummaryController {

    @Autowired
    private ClientFeedbackSummaryService summaryService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<FeedbackSummaryDto> getOverallFeedbackSummary() {
        FeedbackSummaryDto summary = summaryService.getOverallFeedbackSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLIENT')")
    public ResponseEntity<FeedbackSummaryDto> getClientFeedbackSummary(@PathVariable Long clientId) {
        FeedbackSummaryDto summary = summaryService.getClientFeedbackSummary(clientId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/site/{siteId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<FeedbackSummaryDto> getSiteFeedbackSummary(@PathVariable Long siteId) {
        FeedbackSummaryDto summary = summaryService.getSiteFeedbackSummary(siteId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/guard/{guardId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<FeedbackSummaryDto> getGuardFeedbackSummary(@PathVariable Long guardId) {
        FeedbackSummaryDto summary = summaryService.getGuardFeedbackSummary(guardId);
        return ResponseEntity.ok(summary);
    }
}
