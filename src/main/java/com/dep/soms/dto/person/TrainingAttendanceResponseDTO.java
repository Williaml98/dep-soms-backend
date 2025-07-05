package com.dep.soms.dto.person;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingAttendanceResponseDTO {
    private Long id;
    private Long trainingSessionId;
    private String trainingSessionTitle;
    private Long trainingRecordId;
    private String participantName;
    private LocalDateTime registeredAt;
    private Boolean attended;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Boolean passed;
    private Integer score;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Session details
    private LocalDate sessionDate;
    private LocalDateTime sessionStartTime;
    private LocalDateTime sessionEndTime;
    private String sessionLocation;
    private String trainerName;
}
