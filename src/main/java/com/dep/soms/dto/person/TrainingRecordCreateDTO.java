package com.dep.soms.dto.person;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingRecordCreateDTO {
    @NotNull(message = "Person registration ID is required")
    private Long personRegistrationId;

    @Size(max = 1000)
    private String trainerNotes;
}
