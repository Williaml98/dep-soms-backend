package com.dep.soms.dto.report;

import com.dep.soms.model.ShiftAssignment;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecordDto {
    private Long assignmentId;
    private Long shiftId;
    private String shiftName;
    private Long guardId;
    private String guardName;
    private Long siteId;
    private String siteName;
    private LocalDate date;
    private LocalTime scheduledStartTime;
    private LocalTime scheduledEndTime;
    private LocalTime actualStartTime;
    private LocalTime actualEndTime;
    private ShiftAssignment.AssignmentStatus status;
    private boolean isLate;
    private boolean isEarlyDeparture;
    private long minutesLate;
    private long minutesEarlyDeparture;
}
