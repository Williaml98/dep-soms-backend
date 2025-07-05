package com.dep.soms.controller;

import com.dep.soms.dto.guard.GuardDto;
import com.dep.soms.dto.guard.GuardRegistrationRequest;
import com.dep.soms.dto.guard.GuardUpdateRequest;
import com.dep.soms.dto.person.PersonRegistrationDto;
import com.dep.soms.model.PersonRegistration;
import com.dep.soms.service.GuardService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/guards")
@Slf4j
public class GuardController {
    @Autowired
    private GuardService guardService;


    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<GuardDto>> getAllGuards() {
        List<GuardDto> guards = guardService.getAllGuards();
        return ResponseEntity.ok(guards);
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLIENT')")
    public ResponseEntity<List<GuardDto>> getGuardsByClientId(@PathVariable Long clientId) {
        List<GuardDto> guards = guardService.getGuardsByClientId(clientId);
        return ResponseEntity.ok(guards);
    }

    // Add this new endpoint that includes GUARD role
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('GUARD')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<GuardDto>> getAllGuardsIncludingForGuards() {
        List<GuardDto> guards = guardService.getAllGuards();
        return ResponseEntity.ok(guards);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('GUARD')")
    public ResponseEntity<GuardDto> getGuardById(@PathVariable Long id) {
        GuardDto guard = guardService.getGuardById(id);
        return ResponseEntity.ok(guard);
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<GuardDto> registerGuard(@Valid @RequestBody GuardRegistrationRequest registrationRequest) {
        GuardDto createdGuard = guardService.registerGuard(registrationRequest);
        return ResponseEntity.ok(createdGuard);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<GuardDto> updateGuard(
            @PathVariable Long id,
            @Valid @RequestBody GuardUpdateRequest updateRequest) {
        GuardDto updatedGuard = guardService.updateGuard(id, updateRequest);
        return ResponseEntity.ok(updatedGuard);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteGuard(@PathVariable Long id) {
        guardService.deleteGuard(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/available")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<GuardDto>> getAvailableGuards(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String shiftId) {
        List<GuardDto> availableGuards = guardService.getAvailableGuards(date, shiftId);
        return ResponseEntity.ok(availableGuards);
    }


    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('GUARD')")
    public ResponseEntity<GuardDto> getGuardByUserId(@PathVariable Long userId) {
        try {
            GuardDto guard = guardService.getGuardByUserId(userId);
            return ResponseEntity.ok(guard);
        } catch (Exception e) {
            System.err.println("Error fetching guard for user ID: " + userId + " - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }


    /**
     * Get all person registrations that are approved for guard
     */
//    @GetMapping("/approved-persons")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
//    public ResponseEntity<List<PersonRegistration>> getApprovedForGuardPersons() {
//        List<PersonRegistration> persons = guardService.getApprovedForGuardPersons();
//        return ResponseEntity.ok(persons);
//    }

    // Update this method in GuardController.java
    @GetMapping("/approved-persons")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> getApprovedForGuardPersons() {
        try {
            List<PersonRegistrationDto> persons = guardService.getApprovedForGuardPersons();
            return ResponseEntity.ok(persons);
        } catch (Exception e) {
            log.error("Error fetching approved persons", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to fetch approved persons: " + e.getMessage()));
        }
    }


    /**
     * Create a guard from an approved person registration
     */
    @PostMapping("/create-from-person/{personId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<GuardDto> createGuardFromPerson(@PathVariable Long personId) {
        GuardDto guard = guardService.createGuardFromPersonRegistration(personId);
        return ResponseEntity.ok(guard);
    }

    /**
     * Soft delete a guard (set status to INACTIVE)
     */
    @DeleteMapping("/soft-delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> softDeleteGuard(@PathVariable Long id) {
        guardService.softDeleteGuard(id);
        return ResponseEntity.ok().build();
    }

}
