package com.dep.soms.controller;


import com.dep.soms.dto.person.TrainingRecordResponseDTO;
import com.dep.soms.dto.person.TrainingRecordUpdateDTO;
import com.dep.soms.model.TrainingRecord;
import com.dep.soms.service.TrainingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/training/records")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class TrainingRecordController {
    private final TrainingService trainingService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TRAINER') or hasRole('HR')")
    public ResponseEntity<List<TrainingRecordResponseDTO>> getAllTrainingRecords() {
        return ResponseEntity.ok(trainingService.getAllTrainingRecords());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TRAINER') or hasRole('HR')")
    public ResponseEntity<TrainingRecordResponseDTO> getTrainingRecordById(@PathVariable Long id) {
        return ResponseEntity.ok(trainingService.getTrainingRecordById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TRAINER') or hasRole('HR')")
    public ResponseEntity<TrainingRecordResponseDTO> updateTrainingRecord(
            @PathVariable Long id,
            @Valid @RequestBody TrainingRecordUpdateDTO recordDTO) {
        return ResponseEntity.ok(trainingService.updateTrainingRecord(id, recordDTO));
    }

    @GetMapping("/by-person/{personId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TRAINER') or hasRole('HR')")
    public ResponseEntity<TrainingRecordResponseDTO> getTrainingRecordByPerson(@PathVariable Long personId) {
        return ResponseEntity.ok(trainingService.getTrainingRecordByPerson(personId));
    }

    @GetMapping("/by-status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TRAINER') or hasRole('HR')")
    public ResponseEntity<List<TrainingRecordResponseDTO>> getTrainingRecordsByStatus(
            @PathVariable TrainingRecord.TrainingStatus status) {
        return ResponseEntity.ok(trainingService.getTrainingRecordsByStatus(status));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TRAINER') or hasRole('HR')")
    public ResponseEntity<TrainingRecordResponseDTO> completeTrainingRecord(
            @PathVariable Long id,
            @RequestParam(required = false) Boolean passed) {
        return ResponseEntity.ok(trainingService.completeTrainingRecord(id, passed));
    }

    @PatchMapping("/{id}/issue-certifications")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('HR')")
    public ResponseEntity<TrainingRecordResponseDTO> issueCertifications(@PathVariable Long id) {
        return ResponseEntity.ok(trainingService.issueCertifications(id));
    }
}
