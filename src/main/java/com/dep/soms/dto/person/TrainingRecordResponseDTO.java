package com.dep.soms.dto.person;

import com.dep.soms.model.TrainingRecord.TrainingStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingRecordResponseDTO {
    private Long id;
    private Long personRegistrationId;
    private String personName;
    private LocalDateTime enrolledAt;
    private LocalDateTime completedAt;
    private Integer overallScore;
    private Boolean passed;
    private TrainingStatus trainingStatus;
    private String trainerNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Related data
    private List<TrainingAttendanceResponseDTO> attendances;
    private List<TrainingCertificationResponseDTO> certifications;

    // Summary fields
    private int totalSessionsAttended;
    private int totalSessionsRequired;
    private double attendancePercentage;
}
