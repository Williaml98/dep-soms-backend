package com.dep.soms.dto.patrol;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkPatrolResponse {
    private List<PatrolDto> createdPatrols;
    private List<String> warnings;
    private List<String> errors;
    private int totalPatrolsCreated;
}

