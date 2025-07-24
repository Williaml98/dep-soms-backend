package com.dep.soms.dto.patrol;

import com.dep.soms.model.Patrol;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatrolDto {
    private Long id;
    private Long supervisorId;
    private String supervisorName;
    private Long siteId;
    private String siteName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Patrol.PatrolStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CheckpointDto> checkpoints;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckpointDto {
        private Long id;
        private Long patrolPointId;
        private String patrolPointName;
        private LocalDateTime checkTime;
        private Double latitude;
        private Double longitude;
        private Double expectedLatitude;
        private Double expectedLongitude;
        private String notes;
        private String photoPath;
    }
}
