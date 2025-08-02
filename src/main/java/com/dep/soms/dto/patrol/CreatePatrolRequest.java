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

    // Add patrol points to be created for each site
    private List<PatrolPointRequest> patrolPoints;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatrolPointRequest {
        private Long siteId; // Which site this point belongs to
        private String name;
        private String description;
        private Double latitude;
        private Double longitude;
        private Integer sequenceNumber;
        private LocalTime expectedCheckTime; // Optional expected check time
    }
}
