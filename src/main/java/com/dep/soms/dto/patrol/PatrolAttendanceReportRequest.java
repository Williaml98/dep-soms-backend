package com.dep.soms.dto.patrol;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PatrolAttendanceReportRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Long> supervisorIds;
    private List<Long> siteIds;
    private List<Long> patrolIds;
    private String status;
}