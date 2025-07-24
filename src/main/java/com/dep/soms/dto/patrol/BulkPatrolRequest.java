package com.dep.soms.dto.patrol;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkPatrolRequest {
    private List<Long> supervisorIds;
    private List<Long> siteIds;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String notes;
}

