package com.dep.soms.controller;


import com.dep.soms.dto.person.TrainingAttendanceCreateDTO;
import com.dep.soms.dto.person.TrainingAttendanceResponseDTO;
import com.dep.soms.dto.person.TrainingAttendanceUpdateDTO;
import com.dep.soms.exception.ResourceNotFoundException;
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
@RequestMapping("/api/training/attendances")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class TrainingAttendanceController {
    private final TrainingService trainingService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SUPERVISOR') or hasRole('HR')")
    public ResponseEntity<List<TrainingAttendanceResponseDTO>> getAllAttendances() {
        return ResponseEntity.ok(trainingService.getAllAttendances());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SUPERVISOR') or hasRole('HR')")
    public ResponseEntity<TrainingAttendanceResponseDTO> getAttendanceById(@PathVariable Long id) {
        return ResponseEntity.ok(trainingService.getAttendanceById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SUPERVISOR') or hasRole('HR')")
    public ResponseEntity<TrainingAttendanceResponseDTO> createAttendance(
            @Valid @RequestBody TrainingAttendanceCreateDTO attendanceDTO) {
        return new ResponseEntity<>(trainingService.createTrainingAttendance(attendanceDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SUPERVISOR') or hasRole('HR')")
    public ResponseEntity<TrainingAttendanceResponseDTO> updateAttendance(
            @PathVariable Long id,
            @Valid @RequestBody TrainingAttendanceUpdateDTO attendanceDTO) {
        return ResponseEntity.ok(trainingService.updateAttendance(id, attendanceDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SUPERVISOR') or hasRole('HR')")
    public ResponseEntity<Void> deleteAttendance(@PathVariable Long id) {
        trainingService.deleteAttendance(id);
        return ResponseEntity.noContent().build();
    }

//    @GetMapping("/by-session/{sessionId}")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TRAINER') or hasRole('SUPERVISOR') or hasRole('HR')")
//    public ResponseEntity<List<TrainingAttendanceResponseDTO>> getAttendancesBySession(@PathVariable Long sessionId) {
//        return ResponseEntity.ok(trainingService.getAttendancesBySession(sessionId));
//    }

    @GetMapping("/by-session/{sessionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TRAINER') or hasRole('SUPERVISOR') or hasRole('HR')")
    public ResponseEntity<List<TrainingAttendanceResponseDTO>> getAttendancesBySession(@PathVariable Long sessionId) {
        System.out.println("DEBUG: Controller - getAttendancesBySession called with sessionId: " + sessionId);
        try {
            List<TrainingAttendanceResponseDTO> attendances = trainingService.getAttendancesBySession(sessionId);
            System.out.println("DEBUG: Controller - Successfully retrieved " + attendances.size() + " attendance records");
            return ResponseEntity.ok(attendances);
        } catch (ResourceNotFoundException e) {
            System.out.println("ERROR: Controller - ResourceNotFoundException: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("ERROR: Controller - Unexpected exception: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }


    @GetMapping("/by-person/{personId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TRAINER') or hasRole('SUPERVISOR') or hasRole('HR')")
    public ResponseEntity<List<TrainingAttendanceResponseDTO>> getAttendancesByPerson(@PathVariable Long personId) {
        return ResponseEntity.ok(trainingService.getAttendancesByPerson(personId));
    }

    @PatchMapping("/{id}/check-in")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TRAINER') or hasRole('SUPERVISOR') or hasRole('HR')")
    public ResponseEntity<TrainingAttendanceResponseDTO> checkInAttendance(@PathVariable Long id) {
        return ResponseEntity.ok(trainingService.checkInAttendance(id));
    }

    @PatchMapping("/{id}/check-out")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TRAINER') or hasRole('SUPERVISOR') or hasRole('HR')")
    public ResponseEntity<TrainingAttendanceResponseDTO> checkOutAttendance(@PathVariable Long id) {
        return ResponseEntity.ok(trainingService.checkOutAttendance(id));
    }

    @PatchMapping("/{id}/score")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TRAINER') or hasRole('SUPERVISOR') or hasRole('HR')")
    public ResponseEntity<TrainingAttendanceResponseDTO> updateAttendanceScore(
            @PathVariable Long id,
            @RequestParam Integer score,
            @RequestParam(required = false) Boolean passed) {
        return ResponseEntity.ok(trainingService.updateAttendanceScore(id, score, passed));
    }

//    @GetMapping("/by-date/{date}")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TRAINER') or hasRole('SUPERVISOR') or hasRole('HR')")
//    public ResponseEntity<List<TrainingAttendanceResponseDTO>> getAttendancesByDate(
//            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
//
//        return ResponseEntity.ok(trainingService.getAttendancesByDate(date));
//    }

    @GetMapping("/by-date/{date}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TRAINER') or hasRole('SUPERVISOR') or hasRole('HR')")
    public ResponseEntity<List<TrainingAttendanceResponseDTO>> getAttendancesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long sessionId) {

        System.out.println("DEBUG: [Controller] Received date: " + date + ", sessionId: " + sessionId);

        try {
            List<TrainingAttendanceResponseDTO> attendances = trainingService.getAttendancesByDate(date, sessionId);
            System.out.println("DEBUG: [Controller] Returning " + attendances.size() + " records");
            return ResponseEntity.ok(attendances);
        } catch (Exception e) {
            System.out.println("ERROR: [Controller] " + e.getMessage());
            throw e;
        }
    }

}
