package com.dep.soms.dto.patrol;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatrolAssignmentRequest {
    private Long patrolId;
    private Long supervisorId;

}