package com.dep.soms.dto.attendance;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class AttendanceSummaryDto {
    private Long guardId;
    private String guardName;
    private int totalShifts;
    private int presentCount;
    private int absentCount;
    private int lateCount;
    private int leftEarlyCount;
    private int incompleteCount;
    private Duration totalHoursWorked;
    private Map<String, Integer> statusCounts;
    private List<AttendanceRecordDto> records;
}
