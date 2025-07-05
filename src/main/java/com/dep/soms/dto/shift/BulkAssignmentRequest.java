package com.dep.soms.dto.shift;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkAssignmentRequest {
    private Long shiftId;
    private List<Long> guardIds;
    private LocalDate startDate;
    private LocalDate endDate;
    private String daysOfWeek; // Comma-separated day numbers (1-7)
    private String notes;
    private boolean overrideExisting;
    private boolean checkQualifications;
    private boolean respectPreferences;
}
