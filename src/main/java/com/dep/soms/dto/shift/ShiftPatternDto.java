package com.dep.soms.dto.shift;

import com.dep.soms.model.ShiftPattern;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftPatternDto {
    private Long id;
    private String name;
    private String description;
    private String patternDefinition;
    private ShiftPattern.PatternType patternType;
    private Integer rotationLength;
    private boolean active;
}
