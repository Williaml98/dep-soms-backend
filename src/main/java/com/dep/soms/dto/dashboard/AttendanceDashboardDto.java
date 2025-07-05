package com.dep.soms.dto.dashboard;

import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
public class AttendanceDashboardDto {
    private LocalDate date;
    private int totalAssignments;
    private int presentCount;
    private int absentCount;
    private int incompleteCount;
    private int activeGuardsCount;
    private int lateGuardsCount;
    private double attendanceRate;
    private Map<String, Long> siteCounts;
    private Map<String, Long> statusCounts;
}
