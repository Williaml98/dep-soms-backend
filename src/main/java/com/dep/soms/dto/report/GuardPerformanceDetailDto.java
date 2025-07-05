package com.dep.soms.dto.report;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuardPerformanceDetailDto {
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private int totalShifts;
    private int completedShifts;
    private int missedShifts;
    private int lateArrivals;
    private double attendanceRate;
    private int incidents;
}
