//package com.dep.soms.controller;
//
//import com.dep.soms.dto.patrol.*;
//import com.dep.soms.model.User;
//import com.dep.soms.service.SupervisorPatrolService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@CrossOrigin(origins = "*", maxAge = 3600)
//@RestController
//@RequestMapping("/api/supervisor/patrols")
//public class SupervisorPatrolController {
//
//    @Autowired
//    private SupervisorPatrolService supervisorPatrolService;
//
//    private static final Logger logger = LoggerFactory.getLogger(SupervisorPatrolController.class);
//
//    @GetMapping("/today")
//    @PreAuthorize("hasRole('SUPERVISOR')")
//    public ResponseEntity<List<PatrolAssignmentDto>> getTodayAssignments() {
//        Long supervisorId = getCurrentSupervisorId();
//        List<PatrolAssignmentDto> assignments = supervisorPatrolService.getTodayAssignments(supervisorId);
//        return ResponseEntity.ok(assignments);
//    }
//
//    @PostMapping("/assignments/{assignmentId}/accept")
//    @PreAuthorize("hasRole('SUPERVISOR')")
//    public ResponseEntity<PatrolAssignmentDto> acceptAssignment(@PathVariable Long assignmentId) {
//        Long supervisorId = getCurrentSupervisorId();
//        PatrolAssignmentDto assignment = supervisorPatrolService.acceptAssignment(assignmentId, supervisorId);
//        return ResponseEntity.ok(assignment);
//    }
//
//    @PostMapping("/assignments/{assignmentId}/decline")
//    @PreAuthorize("hasRole('SUPERVISOR')")
//    public ResponseEntity<PatrolAssignmentDto> declineAssignment(
//            @PathVariable Long assignmentId,
//            @RequestParam String reason) {
//        Long supervisorId = getCurrentSupervisorId();
//        PatrolAssignmentDto assignment = supervisorPatrolService.declineAssignment(assignmentId, supervisorId, reason);
//        return ResponseEntity.ok(assignment);
//    }
//
//    @PostMapping("/assignments/{assignmentId}/start")
//    @PreAuthorize("hasRole('SUPERVISOR')")
//    public ResponseEntity<PatrolDto> startPatrol(
//            @PathVariable Long assignmentId,
//            @RequestParam(required = false) Double latitude,
//            @RequestParam(required = false) Double longitude) {
//        Long supervisorId = getCurrentSupervisorId();
//        PatrolDto patrol = supervisorPatrolService.startPatrol(assignmentId, supervisorId, latitude, longitude);
//        return ResponseEntity.ok(patrol);
//    }
//
//    @PostMapping("/assignments/{assignmentId}/checkpoints/{checkpointId}")
//    @PreAuthorize("hasRole('SUPERVISOR')")
//    public ResponseEntity<PatrolDto> updateCheckpoint(
//            @PathVariable Long assignmentId,
//            @PathVariable Long checkpointId,
//            @RequestBody CheckpointUpdateRequest request) {
//        Long supervisorId = getCurrentSupervisorId();
//        PatrolDto patrol = supervisorPatrolService.updateCheckpoint(assignmentId, checkpointId, supervisorId, request);
//        return ResponseEntity.ok(patrol);
//    }
//
//    @PostMapping("/assignments/{assignmentId}/complete")
//    @PreAuthorize("hasRole('SUPERVISOR')")
//    public ResponseEntity<PatrolDto> completePatrol(
//            @PathVariable Long assignmentId,
//            @RequestParam(required = false) Double latitude,
//            @RequestParam(required = false) Double longitude,
//            @RequestParam(required = false) String notes) {
//        Long supervisorId = getCurrentSupervisorId();
//        PatrolDto patrol = supervisorPatrolService.completePatrol(assignmentId, supervisorId, latitude, longitude, notes);
//        return ResponseEntity.ok(patrol);
//    }
//
//    private Long getCurrentSupervisorId() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null || !authentication.isAuthenticated()) {
//            throw new SecurityException("No authenticated user found");
//        }
//        return ((User) authentication.getPrincipal()).getId();
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<String> handleException(Exception e) {
//        logger.error("Error in SupervisorPatrolController: ", e);
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body("An error occurred: " + e.getMessage());
//    }
//
//    // Get all patrols (for admin/managers)
//    @GetMapping("/all")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
//    public ResponseEntity<List<PatrolDto>> getAllPatrols() {
//        List<PatrolDto> patrols = supervisorPatrolService.getAllPatrols();
//        return ResponseEntity.ok(patrols);
//    }
//
//    // Get patrols assigned to current supervisor
//    @GetMapping("/my-patrols")
//    @PreAuthorize("hasRole('SUPERVISOR')")
//    public ResponseEntity<List<PatrolDto>> getMyPatrols() {
//        Long supervisorId = getCurrentSupervisorId();
//        List<PatrolDto> patrols = supervisorPatrolService.getPatrolsBySupervisor(supervisorId);
//        return ResponseEntity.ok(patrols);
//    }
//
//    @GetMapping("/in-progress")
//    @PreAuthorize("hasRole('SUPERVISOR')")
//    public ResponseEntity<List<PatrolAssignmentDto>> getInProgressAssignments() {
//        Long supervisorId = getCurrentSupervisorId();
//        List<PatrolAssignmentDto> assignments = supervisorPatrolService.getInProgressAssignments(supervisorId);
//        return ResponseEntity.ok(assignments);
//    }
//
//    // Rest of the existing endpoints (accept, decline, start, update checkpoint, complete)
//    // ...
//
//
//
//}

package com.dep.soms.controller;

import com.dep.soms.dto.patrol.*;
import com.dep.soms.service.SupervisorPatrolService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/supervisor/patrols")
public class SupervisorPatrolController {

    @Autowired
    private SupervisorPatrolService supervisorPatrolService;

    private static final Logger logger = LoggerFactory.getLogger(SupervisorPatrolController.class);

    @GetMapping("/today")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<List<PatrolAssignmentDto>> getTodayAssignments(
            @RequestParam Long supervisorId) {
        List<PatrolAssignmentDto> assignments = supervisorPatrolService.getTodayAssignments(supervisorId);
        return ResponseEntity.ok(assignments);
    }

    @PostMapping("/assignments/{assignmentId}/accept")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<PatrolAssignmentDto> acceptAssignment(
            @PathVariable Long assignmentId,
            @RequestParam Long supervisorId) {
        PatrolAssignmentDto assignment = supervisorPatrolService.acceptAssignment(assignmentId, supervisorId);
        return ResponseEntity.ok(assignment);
    }

    @PostMapping("/assignments/{assignmentId}/decline")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<PatrolAssignmentDto> declineAssignment(
            @PathVariable Long assignmentId,
            @RequestParam Long supervisorId,
            @RequestParam String reason) {
        PatrolAssignmentDto assignment = supervisorPatrolService.declineAssignment(assignmentId, supervisorId, reason);
        return ResponseEntity.ok(assignment);
    }


    @PostMapping("/assignments/{assignmentId}/start")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<PatrolAssignmentDto> startPatrol(
            @PathVariable Long assignmentId,
            @RequestParam Long supervisorId,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {

        PatrolAssignmentDto assignment = supervisorPatrolService.startPatrol(assignmentId, supervisorId, latitude, longitude);
        return ResponseEntity.ok(assignment);
    }

    @PostMapping("/assignments/{assignmentId}/checkpoints/{checkpointId}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<PatrolDto> updateCheckpoint(
            @PathVariable Long assignmentId,
            @PathVariable Long checkpointId,
            @RequestParam Long supervisorId,
            @RequestBody CheckpointUpdateRequest request) {
        PatrolDto patrol = supervisorPatrolService.updateCheckpoint(assignmentId, checkpointId, supervisorId, request);
        return ResponseEntity.ok(patrol);
    }

    @PostMapping("/assignments/{assignmentId}/complete")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<PatrolDto> completePatrol(
            @PathVariable Long assignmentId,
            @RequestParam Long supervisorId,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) String notes) {
        PatrolDto patrol = supervisorPatrolService.completePatrol(assignmentId, supervisorId, latitude, longitude, notes);
        return ResponseEntity.ok(patrol);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        logger.error("Error in SupervisorPatrolController: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred: " + e.getMessage());
    }

    // Get all patrols (for admin/managers)
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<PatrolDto>> getAllPatrols() {
        List<PatrolDto> patrols = supervisorPatrolService.getAllPatrols();
        return ResponseEntity.ok(patrols);
    }


    @GetMapping("/my-patrols")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<List<PatrolAssignmentDto>> getMyAssignments(
            @RequestParam Long supervisorId) {
        List<PatrolAssignmentDto> assignments = supervisorPatrolService.getAllAssignmentsBySupervisor(supervisorId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/in-progress")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<List<PatrolAssignmentDto>> getInProgressAssignments(
            @RequestParam Long supervisorId) {
        List<PatrolAssignmentDto> assignments = supervisorPatrolService.getInProgressAssignments(supervisorId);
        return ResponseEntity.ok(assignments);
    }

//    @PostMapping("/{assignmentId}/sites/{siteId}/check")
//    @PreAuthorize("hasRole('SUPERVISOR')")
//    public ResponseEntity<SiteCheckResponse> checkSite(
//            @PathVariable Long assignmentId,
//            @PathVariable Long siteId,
//            @RequestParam Long supervisorId,
//            @Valid @RequestBody SiteCheckRequest request) {
//        SiteCheckResponse response = supervisorPatrolService.checkSite(assignmentId, siteId, supervisorId, request);
//        return ResponseEntity.ok(response);
//    }

    @PostMapping("/assignments/{assignmentId}/sites/{siteId}/check")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<SiteCheckResponse> checkSite(
            @PathVariable Long assignmentId,
            @PathVariable Long siteId,
            @RequestParam Long supervisorId,
            @RequestBody @Valid SiteCheckRequest request) {

        logger.info("CheckSite request - assignmentId: {}, siteId: {}, supervisorId: {}",
                assignmentId, siteId, supervisorId);
        logger.debug("Request body: {}", request);

        try {
            SiteCheckResponse response = supervisorPatrolService.checkSite(
                    assignmentId, siteId, supervisorId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error in checkSite endpoint", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}