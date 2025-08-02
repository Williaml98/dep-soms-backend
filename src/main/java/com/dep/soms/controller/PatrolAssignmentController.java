package com.dep.soms.controller;

import com.dep.soms.dto.patrol.*;
import com.dep.soms.model.Patrol;
import com.dep.soms.service.PatrolAssignmentQueryService;
import com.dep.soms.service.PatrolAssignmentService;
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
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/patrol-assignments")
public class PatrolAssignmentController {
    @Autowired
    private PatrolService patrolService;

    @Autowired
    private PatrolAssignmentService patrolAssignmentService;

    private static final Logger logger = LoggerFactory.getLogger(PatrolAssignmentController.class);

    private final PatrolAssignmentQueryService patrolAssignmentQueryService;

    @Autowired
    public PatrolAssignmentController(PatrolAssignmentQueryService patrolAssignmentQueryService) {
        this.patrolAssignmentQueryService = patrolAssignmentQueryService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<PatrolAssignmentDto>> getAllAssignments() {
        List<PatrolAssignmentDto> assignments = patrolAssignmentQueryService.getAllAssignments();
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/patrols/all")
    public ResponseEntity<List<PatrolDto>> getAllPatrolsFromAssignments() {
        List<PatrolDto> patrols = patrolAssignmentQueryService.getAllPatrolsFromAssignments();
        return ResponseEntity.ok(patrols);
    }

    @GetMapping("/patrols/site/{siteId}")
    public ResponseEntity<List<PatrolDto>> getPatrolsBySiteIdFromAssignments(@PathVariable Long siteId) {
        List<PatrolDto> patrols = patrolAssignmentQueryService.getPatrolsBySiteIdFromAssignments(siteId);
        return ResponseEntity.ok(patrols);
    }

    @GetMapping("/patrols/time-range")
    public ResponseEntity<List<PatrolDto>> getPatrolsByTimeRangeFromAssignments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<PatrolDto> patrols = patrolAssignmentQueryService.getPatrolsByTimeRangeFromAssignments(start, end);
        return ResponseEntity.ok(patrols);
    }

    @GetMapping("/patrols/supervisor/{supervisorId}")
    public ResponseEntity<List<PatrolDto>> getPatrolsBySupervisorId(@PathVariable Long supervisorId) {
        List<PatrolDto> patrols = patrolAssignmentQueryService.getPatrolsBySupervisorId(supervisorId);
        return ResponseEntity.ok(patrols);
    }

    @GetMapping("/patrols/supervisor/{supervisorId}/status/{status}")
    public ResponseEntity<List<PatrolDto>> getPatrolsBySupervisorAndStatus(
            @PathVariable Long supervisorId,
            @PathVariable Patrol.PatrolStatus status) {
        List<PatrolDto> patrols = patrolAssignmentQueryService.getPatrolsBySupervisorAndStatus(supervisorId, status);
        return ResponseEntity.ok(patrols);
    }

    @GetMapping("/patrols/active")
    public ResponseEntity<List<PatrolDto>> getActivePatrolsFromAssignments() {
        List<PatrolDto> patrols = patrolAssignmentQueryService.getActivePatrolsFromAssignments();
        return ResponseEntity.ok(patrols);
    }

    @GetMapping("/patrols/{id}")
    public ResponseEntity<PatrolDto> getPatrolByIdFromAssignments(@PathVariable Long id) {
        PatrolDto patrol = patrolAssignmentQueryService.getPatrolByIdFromAssignments(id);
        return ResponseEntity.ok(patrol);
    }

    @PostMapping("/{assignmentId}/accept")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<PatrolAssignmentDto> acceptAssignment(@PathVariable Long assignmentId) {
        PatrolAssignmentDto assignment = patrolService.acceptAssignment(assignmentId);
        return ResponseEntity.ok(assignment);
    }

    @PostMapping("/{assignmentId}/decline")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<PatrolAssignmentDto> declineAssignment(
            @PathVariable Long assignmentId,
            @RequestParam String reason) {
        PatrolAssignmentDto assignment = patrolService.declineAssignment(assignmentId, reason);
        return ResponseEntity.ok(assignment);
    }

    @PostMapping("/{patrolId}/checkpoints/{checkpointId}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<PatrolDto> updateAssignmentCheckpoint(
            @PathVariable Long patrolId,
            @PathVariable Long checkpointId,
            @Valid @RequestBody CheckpointUpdateRequest request) {
        PatrolDto patrol = patrolService.updateCheckpoint(patrolId, checkpointId, request);
        return ResponseEntity.ok(patrol);
    }

    @PostMapping("/{patrolId}/complete")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<PatrolDto> completeAssignment(
            @PathVariable Long patrolId,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) String notes) {
        PatrolDto patrol = patrolService.completePatrol(patrolId, latitude, longitude, notes);
        return ResponseEntity.ok(patrol);
    }


//    @PostMapping("/assign")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
//    public ResponseEntity<PatrolAssignmentDto> createAssignment(@Valid @RequestBody CreatePatrolAssignmentRequest request) {
//        PatrolAssignmentDto assignment = patrolService.createAssignment(request);
//        return ResponseEntity.ok(assignment);
//    }

//    @PostMapping("/bulk-create")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
//    public ResponseEntity<BulkPatrolAssignmentResponse> createBulkAssignments(
//            @Valid @RequestBody BulkPatrolAssignmentRequest request) {
//        BulkPatrolAssignmentResponse response = patrolService.createBulkPatrolAssignments(request);
//
//        if (response.getErrors().isEmpty()) {
//            return ResponseEntity.ok(response);
//        } else if (response.getCreatedAssignments().isEmpty()) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//        } else {
//            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(response);
//        }
//    }

    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<PatrolAssignmentDto> assignSupervisorToPatrol(
            @Valid @RequestBody CreatePatrolAssignmentRequest request) {
        PatrolAssignmentDto assignment = patrolAssignmentService.createAssignment(request);
        return ResponseEntity.ok(assignment);
    }
    @PostMapping("/bulk-create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<BulkPatrolAssignmentResponse> bulkAssignSupervisorsToPatrol(
            @Valid @RequestBody BulkPatrolAssignmentRequest request) {
        BulkPatrolAssignmentResponse response = patrolAssignmentService.createBulkPatrolAssignments(request);

        if (response.getErrors().isEmpty()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(response);
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        logger.error("Error in PatrolAssignmentController: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred: " + e.getMessage());
    }

    // DTO for assignment statistics
    public static class AssignmentStatsDto {
        private long activeAssignments;
        private long completedAssignments;
        private long upcomingAssignments;

        public AssignmentStatsDto(long activeAssignments, long completedAssignments, long upcomingAssignments) {
            this.activeAssignments = activeAssignments;
            this.completedAssignments = completedAssignments;
            this.upcomingAssignments = upcomingAssignments;
        }

        // Getters
        public long getActiveAssignments() { return activeAssignments; }
        public long getCompletedAssignments() { return completedAssignments; }
        public long getUpcomingAssignments() { return upcomingAssignments; }
    }
}