package com.dep.soms.controller;


import com.dep.soms.dto.person.TrainingCertificationResponseDTO;
import com.dep.soms.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/training/certifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class TrainingCertificationController {
    private final TrainingService trainingService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TRAINER')")
    public ResponseEntity<List<TrainingCertificationResponseDTO>> getAllTrainingCertifications() {
        return ResponseEntity.ok(trainingService.getAllTrainingCertifications());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TRAINER')")
    public ResponseEntity<TrainingCertificationResponseDTO> getTrainingCertificationById(@PathVariable Long id) {
        return ResponseEntity.ok(trainingService.getTrainingCertificationById(id));
    }

    @GetMapping("/by-record/{recordId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TRAINER')")
    public ResponseEntity<List<TrainingCertificationResponseDTO>> getTrainingCertificationsByRecord(
            @PathVariable Long recordId) {
        return ResponseEntity.ok(trainingService.getTrainingCertificationsByRecord(recordId));
    }

    @GetMapping("/by-person/{personId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TRAINER')")
    public ResponseEntity<List<TrainingCertificationResponseDTO>> getTrainingCertificationsByPerson(
            @PathVariable Long personId) {
        return ResponseEntity.ok(trainingService.getTrainingCertificationsByPerson(personId));
    }

    @GetMapping("/by-certification/{certificationId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TRAINER')")
    public ResponseEntity<List<TrainingCertificationResponseDTO>> getTrainingCertificationsByCertification(
            @PathVariable Long certificationId) {
        return ResponseEntity.ok(trainingService.getTrainingCertificationsByCertification(certificationId));
    }

    @GetMapping("/verify/{certificateNumber}")
    public ResponseEntity<TrainingCertificationResponseDTO> verifyCertificate(
            @PathVariable String certificateNumber) {
        return ResponseEntity.ok(trainingService.verifyCertificate(certificateNumber));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Void> deleteTrainingCertification(@PathVariable Long id) {
        trainingService.deleteTrainingCertification(id);
        return ResponseEntity.noContent().build();
    }
}
