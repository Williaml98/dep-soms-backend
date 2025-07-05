package com.dep.soms.controller;

import com.dep.soms.dto.person.PersonRegistrationRequestDTO;
import com.dep.soms.dto.person.PersonRegistrationResponseDTO;
import com.dep.soms.dto.person.PersonRegistrationSummaryDTO;
import com.dep.soms.dto.person.PersonRegistrationUpdateDTO;
import com.dep.soms.model.PersonRegistration.RegistrationStatus;
import com.dep.soms.model.PersonRegistration.VerificationStatus;
import com.dep.soms.service.PersonRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/person-registrations")
@RequiredArgsConstructor
public class PersonRegistrationController {

    private final PersonRegistrationService personRegistrationService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HR')")
    public ResponseEntity<PersonRegistrationResponseDTO> createRegistration(
            @RequestPart("request") PersonRegistrationRequestDTO requestDTO,
            @RequestPart(value = "resumeCv", required = false) MultipartFile resumeCv,
            @RequestPart(value = "nationalIdCopy", required = false) MultipartFile nationalIdCopy,
            @RequestPart(value = "passportCopy", required = false) MultipartFile passportCopy,
            @RequestPart(value = "driversLicenseCopy", required = false) MultipartFile driversLicenseCopy,
            @RequestPart(value = "educationCertificates", required = false) MultipartFile educationCertificates,
            @RequestPart(value = "professionalCertificatesFiles", required = false) MultipartFile professionalCertificatesFiles,
            @RequestPart(value = "medicalCertificate", required = false) MultipartFile medicalCertificate,
            @RequestPart(value = "referenceLetters", required = false) MultipartFile referenceLetters,
            @RequestPart(value = "otherDocuments", required = false) MultipartFile otherDocuments) throws IOException {

        PersonRegistrationResponseDTO response = personRegistrationService.createRegistration(
                requestDTO,
                resumeCv,
                nationalIdCopy,
                passportCopy,
                driversLicenseCopy,
                educationCertificates,
                professionalCertificatesFiles,
                medicalCertificate,
                referenceLetters,
                otherDocuments
        );

        return ResponseEntity.ok(response);
    }


    // Basic endpoints
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HR')")
    public ResponseEntity<PersonRegistrationResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(personRegistrationService.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HR')")
    public ResponseEntity<Page<PersonRegistrationSummaryDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(personRegistrationService.findAll(pageable));
    }

    // Search endpoints
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HR')")
    public ResponseEntity<Page<PersonRegistrationSummaryDTO>> search(
            @RequestParam String term,
            Pageable pageable) {
        return ResponseEntity.ok(personRegistrationService.search(term, pageable));
    }

    // Status-based endpoints
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HR', 'SUPERVISOR')")
    public ResponseEntity<List<PersonRegistrationSummaryDTO>> getByStatus(
            @PathVariable RegistrationStatus status) {
        return ResponseEntity.ok(personRegistrationService.findByRegistrationStatusWithTrainingDetails(status));
    }

    // Document expiry endpoint - Only Admin and Manager
    @GetMapping("/expiring-documents")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HR')")
    public ResponseEntity<List<PersonRegistrationSummaryDTO>> getExpiringDocuments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(personRegistrationService.findPersonsWithExpiringDocuments(start, end));
    }

    // Dashboard statistics - Admin only
    @GetMapping("/stats/registration")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<RegistrationStatus, Long>> getRegistrationStats() {
        return ResponseEntity.ok(personRegistrationService.getRegistrationStatusCounts());
    }

    // Status update endpoints - Admin and Manager only
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam RegistrationStatus status,
            @RequestParam(required = false) String notes) {
        personRegistrationService.updateRegistrationStatus(id, status, notes);
        return ResponseEntity.noContent().build();
    }

    // Verification status updates - HR only
    @PatchMapping("/{id}/verification")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HR')")
    public ResponseEntity<Void> updateVerification(
            @PathVariable Long id,
            @RequestParam(required = false) VerificationStatus background,
            @RequestParam(required = false) VerificationStatus medical,
            @RequestParam(required = false) VerificationStatus drug,
            @RequestParam(required = false) VerificationStatus reference) {
        personRegistrationService.updateVerificationStatus(id, background, medical, drug, reference);
        return ResponseEntity.noContent().build();
    }

    // Utility endpoints - Public access
    @GetMapping("/exists/email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(personRegistrationService.existsByEmail(email));
    }

    // HR-specific endpoints
    @GetMapping("/verification/pending")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<PersonRegistrationSummaryDTO>> getPendingVerifications() {
        return ResponseEntity.ok(personRegistrationService.findByBackgroundCheckStatus(VerificationStatus.PENDING));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HR')")
    public ResponseEntity<PersonRegistrationResponseDTO> updateRegistration(
            @PathVariable Long id,
            @RequestBody PersonRegistrationUpdateDTO updateDTO) {
        return ResponseEntity.ok(personRegistrationService.updateRegistration(id, updateDTO));
    }
}