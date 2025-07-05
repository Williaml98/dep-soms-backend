package com.dep.soms.dto.person;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingAttendanceUpdateDTO {
    private Boolean attended;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Boolean passed;
    private Integer score;
    private String notes;
}
