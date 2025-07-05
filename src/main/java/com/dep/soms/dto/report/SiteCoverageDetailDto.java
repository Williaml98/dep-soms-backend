package com.dep.soms.dto.report;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteCoverageDetailDto {
    private Long siteId;
    private String siteName;
    private int totalShifts;
    private int totalHours;
    private int coveredHours;
    private double coverageRate;
}
