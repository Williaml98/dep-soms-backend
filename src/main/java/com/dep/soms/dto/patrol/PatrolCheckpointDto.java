package com.dep.soms.dto.patrol;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class PatrolCheckpointDto {
    private Long id;
    private Long patrolPointId;
    private String patrolPointName;
    private LocalTime expectedTime;
    private LocalDateTime actualTime;
    private Double latitude;
    private Double longitude;
    private boolean locationVerified;
    private double distanceFromExpected;
    private String notes;
    private String photoPath;
}
