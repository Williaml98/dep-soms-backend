package com.dep.soms.controller;

import com.dep.soms.dto.shift.ShiftDto;
import com.dep.soms.dto.shift.ShiftRequest;
import com.dep.soms.service.ShiftService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/shifts")
public class ShiftController {

    private static final Logger logger = LoggerFactory.getLogger(ShiftController.class);

    @Autowired
    private ShiftService shiftService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLIENT')")
    public ResponseEntity<List<ShiftDto>> getAllShifts() {
        logger.info("Getting all shifts");
        try {
            List<ShiftDto> shifts = shiftService.getAllShifts();
            logger.info("Found {} shifts", shifts.size());
            return ResponseEntity.ok(shifts);
        } catch (Exception e) {
            logger.error("Error getting all shifts", e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLIENT') or hasRole('GUARD')")
    public ResponseEntity<ShiftDto> getShiftById(@PathVariable Long id) {
        logger.info("Getting shift by id: {}", id);
        try {
            ShiftDto shift = shiftService.getShiftById(id);
            logger.info("Found shift: {}", shift.getName());
            return ResponseEntity.ok(shift);
        } catch (Exception e) {
            logger.error("Error getting shift by id: {}", id, e);
            throw e;
        }
    }

    @GetMapping("/site/{siteId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLIENT') or hasRole('GUARD')")
    public ResponseEntity<List<ShiftDto>> getShiftsBySiteId(@PathVariable Long siteId) {
        logger.info("Getting shifts by site id: {}", siteId);
        try {
            List<ShiftDto> shifts = shiftService.getShiftsBySiteId(siteId);
            logger.info("Found {} shifts for site {}", shifts.size(), siteId);
            return ResponseEntity.ok(shifts);
        } catch (Exception e) {
            logger.error("Error getting shifts by site id: {}", siteId, e);
            throw e;
        }
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ShiftDto> createShift(@Valid @RequestBody ShiftRequest shiftRequest) {
        logger.info("Creating shift: {}", shiftRequest.getShiftName());
        logger.debug("Shift request details: {}", shiftRequest);
        try {
            ShiftDto createdShift = shiftService.createShift(shiftRequest);
            logger.info("Successfully created shift with id: {}", createdShift.getId());
            return ResponseEntity.ok(createdShift);
        } catch (Exception e) {
            logger.error("Error creating shift", e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ShiftDto> updateShift(
            @PathVariable Long id,
            @Valid @RequestBody ShiftRequest shiftRequest) {
        logger.info("Updating shift with id: {}", id);
        try {
            ShiftDto updatedShift = shiftService.updateShift(id, shiftRequest);
            logger.info("Successfully updated shift with id: {}", id);
            return ResponseEntity.ok(updatedShift);
        } catch (Exception e) {
            logger.error("Error updating shift with id: {}", id, e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteShift(@PathVariable Long id) {
        logger.info("Deleting shift with id: {}", id);
        try {
            shiftService.deleteShift(id);
            logger.info("Successfully deleted shift with id: {}", id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting shift with id: {}", id, e);
            throw e;
        }
    }
}
