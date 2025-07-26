package com.dep.soms.dto.patrol;

import lombok.*;

import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePatrolRequest {
    private String name;
    private String description;
    private String startTime; // Format: "HH:mm"
    private String endTime;   // Format: "HH:mm"
    private Long primarySiteId;
    private List<Long> siteIds;
    private Long supervisorId;
    private String patrolType;
    private Integer requiredSupervisors;
    private String colorCode;
    private String notes;
    private Boolean active;
}
