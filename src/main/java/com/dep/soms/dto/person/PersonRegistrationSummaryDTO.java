package com.dep.soms.dto.person;

import com.dep.soms.model.PersonRegistration.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonRegistrationSummaryDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String positionAppliedFor;
    private RegistrationStatus registrationStatus;
    private LocalDate dateOfBirth;
    private Integer age;
    private String nationality;
    private WorkAuthorizationStatus workAuthorizationStatus;

    // Verification Summary
    private VerificationStatus backgroundCheckStatus;
    private VerificationStatus medicalCheckStatus;
    private VerificationStatus drugTestStatus;
    private VerificationStatus referenceCheckStatus;

    // Progress Indicators
    private boolean hasTrainingRecord;
    private boolean isActiveGuard;
    private String trainingStatus;

    private String registeredByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
