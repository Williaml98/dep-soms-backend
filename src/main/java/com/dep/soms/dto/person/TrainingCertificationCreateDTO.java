package com.dep.soms.dto.person;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingCertificationCreateDTO {
    @NotNull(message = "Training record ID is required")
    private Long trainingRecordId;

    @NotNull(message = "Certification ID is required")
    private Long certificationId;

    @NotNull(message = "Issue date is required")
    private LocalDate issuedDate;

    private LocalDate expiryDate;
    private String certificateNumber;
}
