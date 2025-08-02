package com.dep.soms.dto.patrol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePatrolAssignmentRequest {
    private Long supervisorId;
    private LocalDate assignmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String notes;
    private String status;
}
