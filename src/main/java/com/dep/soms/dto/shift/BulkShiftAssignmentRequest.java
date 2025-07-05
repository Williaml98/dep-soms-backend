package com.dep.soms.dto.shift;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkShiftAssignmentRequest {
    private Long shiftId;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<GuardAssignment> guardAssignments;
    private RotationConfig rotationConfig;
    private String notes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuardAssignment {
        private Long guardId; // This is actually user ID from frontend
        private String shiftType; // "MORNING", "EVENING", "NIGHT"
        private LocalTime startTime;
        private LocalTime endTime;
        private List<Integer> daysOfWeek; // 1=Monday, 2=Tuesday, etc.
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RotationConfig {
        private boolean enableRotation;
        private int rotationIntervalWeeks; // Rotate every X weeks
        private String rotationType; // "SHIFT_TYPE", "GUARDS", "BOTH"
    }
}
