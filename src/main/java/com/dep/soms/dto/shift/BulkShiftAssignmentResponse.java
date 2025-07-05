package com.dep.soms.dto.shift;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkShiftAssignmentResponse {
    private int totalAssignmentsCreated;
    private List<ShiftAssignmentDto> createdAssignments;
    private List<String> warnings;
    private List<String> errors;
    private String summary;
}
