package com.dep.soms.controller;

import com.dep.soms.dto.person.TrainingAssignmentRequestDTO;
import com.dep.soms.dto.person.TrainingAttendanceResponseDTO;
import com.dep.soms.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/training/assignments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class TrainingAssignmentController {
    private final TrainingService trainingService;

//    @PostMapping
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SUPERVISOR')")
//    public ResponseEntity<List<TrainingAttendanceResponseDTO>> assignRecruitsToSession(
//            @RequestParam Long sessionId,
//            @RequestBody List<Long> recruitIds) {
//        return new ResponseEntity<>(
//                trainingService.assignRecruitsToTrainingSession(sessionId, recruitIds),
//                HttpStatus.CREATED
//        );
//    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<TrainingAttendanceResponseDTO>> assignRecruitsToSession(
            @RequestBody TrainingAssignmentRequestDTO request) {
        return new ResponseEntity<>(
                trainingService.assignRecruitsToTrainingSession(request.getSessionId(), request.getRecruitIds()),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/by-session/{sessionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<TrainingAttendanceResponseDTO>> getAssignmentsBySession(
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(trainingService.getAttendancesBySession(sessionId));
    }

    @GetMapping("/by-recruit/{recruitId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<TrainingAttendanceResponseDTO>> getAssignmentsByRecruit(
            @PathVariable Long recruitId) {
        return ResponseEntity.ok(trainingService.getAttendancesByPerson(recruitId));
    }
}