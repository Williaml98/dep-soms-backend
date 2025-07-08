package com.dep.soms.controller;


import com.dep.soms.dto.person.TrainingAttendanceResponseDTO;
import com.dep.soms.dto.person.TrainingAttendanceUpdateDTO;
import com.dep.soms.dto.person.TrainingSessionCreateDTO;
import com.dep.soms.dto.person.TrainingSessionResponseDTO;
import com.dep.soms.model.TrainingSession;
import com.dep.soms.service.TrainingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/training/sessions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class TrainingSessionController {
    private final TrainingService trainingService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SUPERVISOR') or hasRole('HR')")
    public ResponseEntity<List<TrainingSessionResponseDTO>> getAllTrainingSessions() {
        return ResponseEntity.ok(trainingService.getAllTrainingSessions());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SUPERVISOR') or hasRole('HR') ")
    public ResponseEntity<TrainingSessionResponseDTO> getTrainingSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(trainingService.getTrainingSessionById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SUPERVISOR') or hasRole('HR')")
    public ResponseEntity<TrainingSessionResponseDTO> createTrainingSession(
            @Valid @RequestBody TrainingSessionCreateDTO trainingSessionDTO) {
        return new ResponseEntity<>(trainingService.createTrainingSession(trainingSessionDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SUPERVISOR') or hasRole('HR')")
    public ResponseEntity<TrainingSessionResponseDTO> updateTrainingSession(
            @PathVariable Long id,
            @Valid @RequestBody TrainingSessionCreateDTO trainingSessionDTO) {
        return ResponseEntity.ok(trainingService.updateTrainingSession(id, trainingSessionDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR') or hasRole('HR')")
    public ResponseEntity<Void> deleteTrainingSession(@PathVariable Long id) {
        trainingService.deleteTrainingSession(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SUPERVISOR') or hasRole('HR')")
    public ResponseEntity<TrainingSessionResponseDTO> updateTrainingSessionStatus(
            @PathVariable Long id,
            @RequestParam TrainingSession.TrainingStatus status) {
        return ResponseEntity.ok(trainingService.updateTrainingSessionStatus(id, status));
    }

    @GetMapping("/upcoming")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SUPERVISOR') or hasRole('HR')")
    public ResponseEntity<List<TrainingSessionResponseDTO>> getUpcomingTrainingSessions() {
        return ResponseEntity.ok(trainingService.getUpcomingTrainingSessions());
    }

    @GetMapping("/by-trainer/{trainerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SUPERVISOR') or hasRole('HR')")
    public ResponseEntity<List<TrainingSessionResponseDTO>> getTrainingSessionsByTrainer(@PathVariable Long trainerId) {
        return ResponseEntity.ok(trainingService.getTrainingSessionsByTrainer(trainerId));
    }

    @GetMapping("/by-status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SUPERVISOR') or hasRole('HR')")
    public ResponseEntity<List<TrainingSessionResponseDTO>> getTrainingSessionsByStatus(
            @PathVariable TrainingSession.TrainingStatus status) {
        return ResponseEntity.ok(trainingService.getTrainingSessionsByStatus(status));
    }




    @PostMapping("/{sessionId}/recruits")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER', 'TRAINER') or hasRole('SUPERVISOR') or hasRole('HR')")
    public ResponseEntity<List<TrainingAttendanceResponseDTO>> assignRecruitsToTrainingSession(
            @PathVariable Long sessionId, @RequestBody List<Long> recruitIds) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(trainingService.assignRecruitsToTrainingSession(sessionId, recruitIds));
    }

    @GetMapping("/{sessionId}/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER', 'TRAINER') or hasRole('SUPERVISOR') or hasRole('HR')")
    public ResponseEntity<List<TrainingAttendanceResponseDTO>> getTrainingSessionAttendance(@PathVariable Long sessionId) {
        return ResponseEntity.ok(trainingService.getTrainingSessionAttendance(sessionId));
    }

    @GetMapping("/{sessionId}/attendance/date/{date}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER', 'TRAINER') or hasRole('SUPERVISOR') or hasRole('HR')")
    public ResponseEntity<List<TrainingAttendanceResponseDTO>> getTrainingSessionAttendanceByDate(
            @PathVariable Long sessionId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(trainingService.getTrainingSessionAttendanceByDate(sessionId, date));
    }

    @PutMapping("/attendance/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER', 'TRAINER') or hasRole('SUPERVISOR') or hasRole('HR')")
    public ResponseEntity<TrainingAttendanceResponseDTO> updateTrainingAttendance(
            @PathVariable Long id, @RequestBody TrainingAttendanceUpdateDTO dto) {
        return ResponseEntity.ok(trainingService.updateTrainingAttendance(id, dto));
    }


    @GetMapping("/ongoing")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER', 'TRAINER') or hasRole('SUPERVISOR') or hasRole('HR')")
    public ResponseEntity<List<TrainingSessionResponseDTO>> getOngoingTrainingSessions() {
        return ResponseEntity.ok(trainingService.getOngoingTrainingSessions());
    }

}
