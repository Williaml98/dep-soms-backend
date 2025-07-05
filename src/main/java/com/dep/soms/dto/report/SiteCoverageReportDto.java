package com.dep.soms.dto.report;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteCoverageReportDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private Long clientId;
    private String clientName;
    private int totalSites;
    private int totalShifts;
    private int totalHours;
    private int coveredHours;
    private double coverageRate;
    private List<SiteCoverageDetailDto> siteDetails;
}
