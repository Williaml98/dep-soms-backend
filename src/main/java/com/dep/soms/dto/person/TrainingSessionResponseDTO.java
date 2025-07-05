//package com.dep.soms.dto.person;
//
//import com.dep.soms.dto.person.TrainingAttendanceResponseDTO;
//import com.dep.soms.model.TrainingSession.TrainingStatus;
//import lombok.*;
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class TrainingSessionResponseDTO {
//    private Long id;
//    private String title;
//    private String description;
//    private Long trainerId;
//    private String trainerName;
//    private LocalDateTime startTime;
//    private LocalDateTime endTime;
//    private String location;
//    private Integer maxParticipants;
//    private TrainingStatus status;
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
//
//    // Related data
//     List<CertificationSummaryDTO> certifications;
//    private List<TrainingAttendanceResponseDTO> attendances;
//
//    // Summary fields
//    private int currentParticipants;
//    private int availableSlots;
//    private boolean isFull;
//    private long durationInHours;
//}

package com.dep.soms.dto.person;

import com.dep.soms.model.TrainingSession.TrainingStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingSessionResponseDTO {
    private Long id;
    private String title;
    private String description;
    private Long trainerId;
    private String trainerName;

    // New fields for multi-day sessions
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime dailyStartTime;
    private LocalTime dailyEndTime;

    // Keep for backward compatibility
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String location;
    private Integer maxParticipants;
    private TrainingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Related data
    private List<CertificationSummaryDTO> certifications;
    private List<TrainingAttendanceResponseDTO> attendances;

    // Summary fields
    private int currentParticipants;
    private int availableSlots;
    private boolean isFull;
    private int durationInDays;
    private long dailyDurationInHours;
}
