package com.dep.soms.dto.patrol;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkPatrolAssignmentResponse {
    private int totalAssignmentsCreated;
    private List<PatrolAssignmentDto> createdAssignments;
    private List<String> warnings;
    private List<String> errors;
    private String summary;
}