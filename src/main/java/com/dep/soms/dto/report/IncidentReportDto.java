package com.dep.soms.dto.report;

import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentReportDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private Long siteId;
    private String siteName;
    private int totalIncidents;
    private Map<String, Integer> incidentsByType;
    private Map<String, Integer> incidentsBySeverity;
    private List<IncidentSummaryDto> incidents;
}
