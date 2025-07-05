package com.dep.soms.controller;

import com.dep.soms.dto.shift.CheckInRequest;
import com.dep.soms.dto.shift.CheckOutRequest;
import com.dep.soms.dto.shift.ShiftAssignmentDto;
import com.dep.soms.service.CheckInOutService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/check")
public class CheckInOutController {
    @Autowired
    private CheckInOutService checkInOutService;

    @PostMapping("/in")
    @PreAuthorize("hasRole('GUARD')")
    public ResponseEntity<ShiftAssignmentDto> checkIn(@Valid @RequestBody CheckInRequest request) {
        ShiftAssignmentDto assignment = checkInOutService.checkIn(request);
        return ResponseEntity.ok(assignment);
    }

    @PostMapping("/out")
    @PreAuthorize("hasRole('GUARD')")
    public ResponseEntity<ShiftAssignmentDto> checkOut(@Valid @RequestBody CheckOutRequest request) {
        ShiftAssignmentDto assignment = checkInOutService.checkOut(request);
        return ResponseEntity.ok(assignment);
    }
}
