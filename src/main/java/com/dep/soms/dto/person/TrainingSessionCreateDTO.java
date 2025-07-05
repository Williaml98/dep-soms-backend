//package com.dep.soms.dto.person;
//
//import com.dep.soms.model.TrainingSession;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.NotNull;
//import jakarta.validation.constraints.Positive;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class TrainingSessionCreateDTO {
//    @NotBlank(message = "Title is required")
//    private String title;
//
//    private String description;
//
//    @NotNull(message = "Trainer ID is required")
//    private Long trainerId;
//
//    @NotNull(message = "Start time is required")
//    private LocalDateTime startTime;
//
//    @NotNull(message = "End time is required")
//    private LocalDateTime endTime;
//
//    @NotBlank(message = "Location is required")
//    private String location;
//
//    @Positive(message = "Maximum participants must be a positive number")
//    private Integer maxParticipants;
//
//    private TrainingSession.TrainingStatus status;
//
//    private List<Long> certificationIds;
//}

package com.dep.soms.dto.person;

import com.dep.soms.model.TrainingSession;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingSessionCreateDTO {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Trainer ID is required")
    private Long trainerId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Daily start time is required")
    private LocalTime dailyStartTime;

    @NotNull(message = "Daily end time is required")
    private LocalTime dailyEndTime;

    @NotBlank(message = "Location is required")
    private String location;

    @Positive(message = "Maximum participants must be a positive number")
    private Integer maxParticipants;

    private TrainingSession.TrainingStatus status;

    private List<Long> certificationIds;
}
