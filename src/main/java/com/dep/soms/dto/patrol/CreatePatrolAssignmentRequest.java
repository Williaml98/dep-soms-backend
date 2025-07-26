
package com.dep.soms.dto.patrol;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePatrolAssignmentRequest {
    private Long patrolId;
    private Long supervisorId;
}