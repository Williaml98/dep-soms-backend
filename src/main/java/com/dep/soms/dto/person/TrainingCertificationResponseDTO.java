package com.dep.soms.dto.person;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingCertificationResponseDTO {
    private Long id;
    private Long trainingRecordId;
    private String participantName;
    private Long certificationId;
    private String certificationName;
    private String certificationDescription;
    private String issuingAuthority;
    private LocalDate issuedDate;
    private LocalDate expiryDate;
    private String certificateNumber;
    private boolean isExpired;
    private int daysUntilExpiry;
    private LocalDateTime createdAt;
}
