package com.dep.soms.dto.shift;

import com.dep.soms.model.ShiftAssignment;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class ShiftAssignmentRequest {
    @NotNull
    private Long shiftId;

    @NotNull
    private Long guardId;

    @NotNull
    private LocalDate date;

    private LocalTime startTime;

    private LocalTime endTime;

    private ShiftAssignment.AssignmentStatus status = ShiftAssignment.AssignmentStatus.SCHEDULED;

    private String notes;
}
