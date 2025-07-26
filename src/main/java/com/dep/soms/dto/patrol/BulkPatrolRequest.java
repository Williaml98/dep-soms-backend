
package com.dep.soms.dto.patrol;

import lombok.*;

import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkPatrolRequest {
    private String name;
    private String description;
    private Long primarySiteId;
    private List<Long> siteIds;
    private List<Long> supervisorIds;
    private String startTime;  // Format: "HH:mm"
    private String endTime;    // Format: "HH:mm"
    private String patrolType;
    private Integer requiredSupervisors;
    private String colorCode;
    private String notes;
    private Boolean active;

    // Rotation configuration
    private RotationConfig rotationConfig;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RotationConfig {
        private Boolean enableRotation;
        private Integer rotationIntervalWeeks;
        private String rotationType; // "PATROL_TYPE", "SUPERVISOR_POSITION", "WEEKLY"
    }
}