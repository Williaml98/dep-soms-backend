package com.dep.soms.dto.patrol;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class PatrolAttendanceSummaryDto {
    private Long supervisorId;
    private String supervisorName;
    private int totalPatrols;
    private int completedCount;
    private int incompleteCount;
    private int missedCount;
    private int poorCompletionCount;
    private int partialCompletionCount;
    private double avgCompletionPercentage;
    private Duration avgPatrolDuration;
    private Map<String, Integer> statusCounts;
    private List<PatrolAttendanceRecordDto> records;
}
