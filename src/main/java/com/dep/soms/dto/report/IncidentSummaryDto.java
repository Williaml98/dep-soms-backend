package com.dep.soms.dto.report;

import com.dep.soms.model.Incident;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentSummaryDto {
    private Long id;
    private String title;
    private String incidentType;
    private LocalDateTime incidentTime;
    private String siteName;
    private String guardName;
    private Incident.IncidentSeverity severity;
    private Incident.IncidentStatus status;
}

