package com.dep.soms.controller;

import com.dep.soms.dto.patrol.*;
import com.dep.soms.service.PatrolService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/patrols")
public class PatrolController {
    @Autowired
    private PatrolService patrolService;

    private static final Logger logger = LoggerFactory.getLogger(PatrolController.class);

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<PatrolDto>> getAllPatrols() {
        List<PatrolDto> patrols = patrolService.getAllPatrols();
        return ResponseEntity.ok(patrols);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<PatrolDto> createPatrol(@Valid @RequestBody CreatePatrolRequest request) {
        PatrolDto patrol = patrolService.createPatrol(request);
        return ResponseEntity.ok(patrol);
    }

    @PostMapping("/bulk-create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<BulkPatrolResponse> createBulkPatrols(@Valid @RequestBody BulkPatrolRequest request) {
        BulkPatrolResponse response = patrolService.createBulkPatrols(request);

        if (response.getErrors().isEmpty()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(response);
        }
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<PatrolDto> startPatrol(
            @PathVariable Long id,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {
        PatrolDto patrol = patrolService.startPatrol(id, latitude, longitude);
        return ResponseEntity.ok(patrol);
    }

    @PostMapping("/{patrolId}/checkpoints/{checkpointId}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<PatrolDto> updateCheckpoint(
            @PathVariable Long patrolId,
            @PathVariable Long checkpointId,
            @Valid @RequestBody CheckpointUpdateRequest request) {
        PatrolDto patrol = patrolService.updateCheckpoint(patrolId, checkpointId, request);
        return ResponseEntity.ok(patrol);
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<PatrolDto> completePatrol(
            @PathVariable Long id,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) String notes) {
        PatrolDto patrol = patrolService.completePatrol(id, latitude, longitude, notes);
        return ResponseEntity.ok(patrol);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> cancelPatrol(
            @PathVariable Long id,
            @RequestParam String reason) {
        patrolService.cancelPatrol(id, reason);
        return ResponseEntity.ok().build();
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        logger.error("Error in PatrolController: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred: " + e.getMessage());
    }
}