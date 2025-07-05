package com.dep.soms.dto.attendance;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ManualAttendanceRequest {
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private String status; // COMPLETED, ABSENT, INCOMPLETE, etc.
    private boolean checkInLocationVerified;
    private boolean checkOutLocationVerified;
    private String adminNotes;
}
