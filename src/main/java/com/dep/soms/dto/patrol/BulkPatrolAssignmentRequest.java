package com.dep.soms.dto.patrol;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkPatrolAssignmentRequest {
    private Long patrolId;
    private List<PatrolAssignment> supervisorAssignments;
    private LocalDate startDate;
    private LocalDate endDate;
    //private String daysOfWeek; // Comma-separated day numbers (1-7)
    //private List<String> daysOfWeek;
    private List<Integer> daysOfWeek;
    private String notes;
    private boolean overrideExisting;
    private boolean checkAvailability;
    private RotationConfig rotationConfig;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatrolAssignment {
        private Long supervisorId;
        private List<Integer> daysOfWeek;// Specific days for this supervisor
        private String patrolType; // "MORNING", "EVENING", etc.
        private String startTime; // Override default patrol start time
        private String endTime;   // Override default patrol end time
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RotationConfig {
        private boolean enableRotation;
        private int rotationIntervalWeeks;
        private String rotationType; // "PATROL_TYPE", "SUPERVISOR_ROTATION", "BOTH"
    }
}