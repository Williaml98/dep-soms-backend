package com.dep.soms.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecordDto {
    private Long id;
    private Long shiftAssignmentId;
    private Long guardId;
    private String guardName;
    private Long siteId;
    private String siteName;
    private Long shiftId;
    private String shiftName;

    private LocalDate date;
    private LocalTime scheduledStartTime;
    private LocalTime scheduledEndTime;

    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    private boolean checkInLocationVerified;
    private boolean checkOutLocationVerified;

    private Duration hoursWorked;
    private String status; // PRESENT, ABSENT, LATE, LEFT_EARLY, INCOMPLETE

    private Long lateByMinutes;
    private Long leftEarlyByMinutes;

    // Additional fields for detailed reporting
    private Double checkInLatitude;
    private Double checkInLongitude;
    private Double checkOutLatitude;
    private Double checkOutLongitude;
    private String notes;
}
