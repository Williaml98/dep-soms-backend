package com.dep.soms.dto.shift;

import com.dep.soms.model.Shift;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Getter
@Setter
public class ShiftRequest {

    @NotNull
    private Long siteId;

    @NotBlank
    private String shiftName;

    private String description;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    @NotNull
    private Integer requiredGuards;

    private boolean active = true;

    private Shift.ShiftType shiftType;

    private String[] daysOfWeek;

    private LocalDate specificDate;

    private Long patternId;

    private Integer breakDurationMinutes;

    private String notes;

    private String colorCode;

    private Set<Long> requiredSkillIds;

    private Integer minimumRestHours;

    // Removed latitude and longitude fields since they come from the site
}
