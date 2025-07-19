package com.dep.soms.controller;

import com.dep.soms.service.TrainingSessionSchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/training-sessions/scheduler")
@RequiredArgsConstructor
public class TrainingSessionSchedulerController {

    private final TrainingSessionSchedulerService trainingSessionSchedulerService;

    /**
     * Endpoint to manually trigger training session status updates for a specific date
     * @param dateStr The date to process (format: yyyy-MM-dd)
     * @return ResponseEntity with status message
     */
    @PostMapping("/update-statuses")
    public ResponseEntity<String> manuallyUpdateSessionStatuses(@RequestParam("date") String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            trainingSessionSchedulerService.manuallyUpdateSessionStatuses(date);
            return ResponseEntity.ok("Successfully triggered training session status updates for " + dateStr);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid date format. Please use yyyy-MM-dd");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to update session statuses: " + e.getMessage());
        }
    }
}