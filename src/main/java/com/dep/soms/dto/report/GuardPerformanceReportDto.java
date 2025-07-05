package com.dep.soms.dto.report;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuardPerformanceReportDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private Long guardId;
    private String guardName;
    private int totalShifts;
    private int completedShifts;
    private int missedShifts;
    private int lateArrivals;
    private int earlyDepartures;
    private double attendanceRate;
    private int totalIncidents;
    private double averageIncidentsPerShift;
    private List<GuardPerformanceDetailDto> performanceByWeek;
}
