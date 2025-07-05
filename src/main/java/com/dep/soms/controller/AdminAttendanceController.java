package com.dep.soms.controller;

import com.dep.soms.dto.attendance.ManualAttendanceRequest;
import com.dep.soms.dto.dashboard.ApiResponse;
import com.dep.soms.exception.ResourceNotFoundException;
import com.dep.soms.model.ShiftAssignment;
import com.dep.soms.repository.ShiftAssignmentRepository;
import com.dep.soms.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/admin/attendance")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAttendanceController {

    @Autowired
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @Autowired
    private AttendanceService attendanceService;

    /**
     * Manually update attendance record (for corrections)
     */
    @PutMapping("/manual-update/{assignmentId}")
    public ResponseEntity<?> manuallyUpdateAttendance(
            @PathVariable Long assignmentId,
            @Valid @RequestBody ManualAttendanceRequest request) {

        ShiftAssignment assignment = shiftAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift assignment not found"));

        // Update check-in time if provided
        if (request.getCheckInTime() != null) {
            assignment.setCheckInTime(request.getCheckInTime());
            assignment.setCheckInLocationVerified(request.isCheckInLocationVerified());
        }

        // Update check-out time if provided
        if (request.getCheckOutTime() != null) {
            assignment.setCheckOutTime(request.getCheckOutTime());
            assignment.setCheckOutLocationVerified(request.isCheckOutLocationVerified());
        }

        // Update status if provided
        if (request.getStatus() != null) {
            assignment.setStatus(ShiftAssignment.AssignmentStatus.valueOf(request.getStatus()));
        }

        // Add admin notes
        if (request.getAdminNotes() != null && !request.getAdminNotes().isEmpty()) {
            String existingNotes = assignment.getNotes() != null ? assignment.getNotes() : "";
            String adminNote = String.format("[ADMIN %s] %s",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    request.getAdminNotes());

            assignment.setNotes(existingNotes + "\n" + adminNote);
        }

        shiftAssignmentRepository.save(assignment);

        return ResponseEntity.ok(new ApiResponse(true, "Attendance record updated successfully"));
    }

    /**
     * Manually mark a guard as present for a shift
     */
    @PutMapping("/mark-present/{assignmentId}")
    public ResponseEntity<?> markGuardPresent(@PathVariable Long assignmentId) {
        ShiftAssignment assignment = shiftAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift assignment not found"));

        // Set check-in and check-out times to shift start and end times
        assignment.setCheckInTime(assignment.getStartTime());
        assignment.setCheckOutTime(assignment.getEndTime());
        assignment.setStatus(ShiftAssignment.AssignmentStatus.COMPLETED);
        assignment.setCheckInLocationVerified(true);
        assignment.setCheckOutLocationVerified(true);

        // Add admin note
        String existingNotes = assignment.getNotes() != null ? assignment.getNotes() : "";
        String adminNote = String.format("[ADMIN %s] Manually marked as present",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        assignment.setNotes(existingNotes + "\n" + adminNote);

        shiftAssignmentRepository.save(assignment);

        return ResponseEntity.ok(new ApiResponse(true, "Guard marked as present"));
    }

    /**
     * Manually mark a guard as absent for a shift
     */
    @PutMapping("/mark-absent/{assignmentId}")
    public ResponseEntity<?> markGuardAbsent(@PathVariable Long assignmentId) {
        ShiftAssignment assignment = shiftAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift assignment not found"));

        // Clear check-in and check-out times
        assignment.setCheckInTime(null);
        assignment.setCheckOutTime(null);
        assignment.setStatus(ShiftAssignment.AssignmentStatus.ABSENT);

        // Add admin note
        String existingNotes = assignment.getNotes() != null ? assignment.getNotes() : "";
        String adminNote = String.format("[ADMIN %s] Manually marked as absent",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        assignment.setNotes(existingNotes + "\n" + adminNote);

        shiftAssignmentRepository.save(assignment);

        return ResponseEntity.ok(new ApiResponse(true, "Guard marked as absent"));
    }
}
