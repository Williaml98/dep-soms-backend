package com.dep.soms.controller;

import com.dep.soms.dto.shift.*;
import com.dep.soms.service.ShiftAssignmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.dep.soms.dto.shift.CheckInOutRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/shift-assignments")
public class ShiftAssignmentController {
    @Autowired
    private ShiftAssignmentService shiftAssignmentService;

    private static final Logger logger = LoggerFactory.getLogger(ShiftAssignmentController.class);

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<ShiftAssignmentDto>> getAllShiftAssignments() {
        List<ShiftAssignmentDto> assignments = shiftAssignmentService.getAllShiftAssignments();
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('GUARD')")
    public ResponseEntity<ShiftAssignmentDto> getShiftAssignmentById(@PathVariable Long id) {
        ShiftAssignmentDto assignment = shiftAssignmentService.getShiftAssignmentById(id);
        return ResponseEntity.ok(assignment);
    }

    @GetMapping("/guard/{guardId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('GUARD')")
    public ResponseEntity<List<ShiftAssignmentDto>> getShiftAssignmentsByGuardId(
            @PathVariable Long guardId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ShiftAssignmentDto> assignments = shiftAssignmentService.getShiftAssignmentsByGuardId(guardId, startDate, endDate);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/guard/{guardId}/current-active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('GUARD')")
    public ResponseEntity<ShiftAssignmentDto> getCurrentActiveShiftByGuard(@PathVariable Long guardId) {
        ShiftAssignmentDto activeShift = shiftAssignmentService.getCurrentActiveShiftByGuard(guardId);
        return ResponseEntity.ok(activeShift);
    }

    @GetMapping("/shift/{shiftId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLIENT')")
    public ResponseEntity<List<ShiftAssignmentDto>> getShiftAssignmentsByShiftId(
            @PathVariable Long shiftId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ShiftAssignmentDto> assignments = shiftAssignmentService.getShiftAssignmentsByShiftId(shiftId, date);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/site/{siteId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLIENT')")
    public ResponseEntity<List<ShiftAssignmentDto>> getShiftAssignmentsBySiteId(
            @PathVariable Long siteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ShiftAssignmentDto> assignments = shiftAssignmentService.getShiftAssignmentsBySiteId(siteId, date);
        return ResponseEntity.ok(assignments);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ShiftAssignmentDto> createShiftAssignment(
            @Valid @RequestBody ShiftAssignmentRequest request) {
        ShiftAssignmentDto createdAssignment = shiftAssignmentService.createShiftAssignment(request);
        return ResponseEntity.ok(createdAssignment);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ShiftAssignmentDto> updateShiftAssignment(
            @PathVariable Long id,
            @Valid @RequestBody ShiftAssignmentRequest request) {
        ShiftAssignmentDto updatedAssignment = shiftAssignmentService.updateShiftAssignment(id, request);
        return ResponseEntity.ok(updatedAssignment);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> deleteShiftAssignment(@PathVariable Long id) {
        shiftAssignmentService.deleteShiftAssignment(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/check-in")
    @PreAuthorize("hasRole('GUARD')")
    public ResponseEntity<LocationVerificationResponse> checkInGuard(
            @PathVariable("id") Long assignmentId,
            @RequestBody CheckInOutRequest request) {
        LocationVerificationResponse response = shiftAssignmentService.checkInGuard(assignmentId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint for guard check-out with location verification
     */
    @PostMapping("/{id}/check-out")
    @PreAuthorize("hasRole('GUARD')")
    public ResponseEntity<LocationVerificationResponse> checkOutGuard(
            @PathVariable("id") Long assignmentId,
            @RequestBody CheckInOutRequest request) {
        LocationVerificationResponse response = shiftAssignmentService.checkOutGuard(assignmentId, request);
        return ResponseEntity.ok(response);
    }


    // Add this method to your existing controller
    @PostMapping("/bulk-create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<BulkShiftAssignmentResponse> createBulkShiftAssignments(
            @RequestBody BulkShiftAssignmentRequest request) {
        try {
            BulkShiftAssignmentResponse response = shiftAssignmentService.createBulkShiftAssignments(request);

            if (response.getErrors().isEmpty()) {
                return ResponseEntity.ok(response);
            } else {
                // Return 207 Multi-Status if there were some errors but some assignments were created
                return ResponseEntity.status(207).body(response);
            }
        } catch (Exception e) {
             logger.error("Error creating bulk shift assignments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BulkShiftAssignmentResponse.builder()
                            .totalAssignmentsCreated(0)
                            .createdAssignments(new ArrayList<>())
                            .warnings(new ArrayList<>())
                            .errors(List.of("Internal server error: " + e.getMessage()))
                            .summary("Bulk assignment failed")
                            .build());
        }
    }



}
