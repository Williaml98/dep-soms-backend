package com.dep.soms.dto.person;

import com.dep.soms.model.TrainingRecord.TrainingStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingRecordSummaryDTO {
    private Long id;
    private LocalDateTime enrolledAt;
    private LocalDateTime completedAt;
    private Integer overallScore;
    private Boolean passed;
    private TrainingStatus trainingStatus;
    private int totalSessionsAttended;
    private int totalCertificationsEarned;
    private double attendancePercentage;
}