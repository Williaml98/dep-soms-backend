package com.dep.soms.dto.attendance;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AttendanceReportRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Long> guardIds;
    private List<Long> siteIds;
    private List<Long> shiftIds;
    private String status; // Optional filter by status
}
