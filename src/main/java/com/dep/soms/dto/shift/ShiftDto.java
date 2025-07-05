package com.dep.soms.dto.shift;

import com.dep.soms.model.Shift;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

@Data
@Builder
public class ShiftDto {

    private Long id;
    private Long siteId;
    private String siteName;
    private String name;
    private String description;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer requiredGuards;
    private boolean active;
    private Shift.ShiftType shiftType;
    private String[] daysOfWeek;
    private LocalDate specificDate;
    private Long patternId;
    private String patternName;
    private Integer breakDurationMinutes;
    private String notes;
    private String colorCode;
    private Set<Long> requiredSkillIds;
    private Set<String> requiredSkillNames;
    private Double latitude;
    private Double longitude;
    private Integer minimumRestHours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
