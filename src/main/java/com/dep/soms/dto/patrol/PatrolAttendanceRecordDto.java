package com.dep.soms.dto.patrol;

import com.dep.soms.dto.patrol.PatrolCheckpointDto;
import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class PatrolAttendanceRecordDto {
    private Long id;
    private Long patrolId;
    private String patrolName;
    private Long supervisorId;
    private String supervisorName;
    private Long siteId;
    private String siteName;
    private LocalDate date;
    private LocalTime scheduledStartTime;
    private LocalTime scheduledEndTime;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private int totalCheckpoints;
    private int completedCheckpoints;
    private double completionPercentage;
    private Duration avgCheckpointTime;
    private String status;
    private List<PatrolCheckpointDto> checkpoints;
}
