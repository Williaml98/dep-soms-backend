//package com.dep.soms.dto.person;
//
//import lombok.*;
//import jakarta.validation.constraints.*;
//
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class TrainingAttendanceCreateDTO {
//    @NotNull(message = "Training session ID is required")
//    private Long trainingSessionId;
//
//    @NotNull(message = "Training record ID is required")
//    private Long trainingRecordId;
//
//    @Size(max = 500)
//    private String notes;
//}

package com.dep.soms.dto.person;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingAttendanceCreateDTO {
    @NotNull(message = "Training session ID is required")
    private Long trainingSessionId;

    @NotNull(message = "Person registration ID is required")
    private Long personRegistrationId;

    private String notes;
}
