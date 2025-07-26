package com.dep.soms.dto.patrol;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatrolDto {
    private Long id;
    private String name;
    private String description;
    private Long supervisorId;
    private String supervisorName;
    private Long primarySiteId;
    private String primarySiteName;
    private List<Long> siteIds;
    private List<String> siteNames;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private String patrolType;
    private Integer requiredSupervisors;
    private String colorCode;
    private String status;
    private String notes;
    private Boolean active;
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
        private String notes;
        private String photoPath;
        private Double expectedLatitude;
        private Double expectedLongitude;
    }
}