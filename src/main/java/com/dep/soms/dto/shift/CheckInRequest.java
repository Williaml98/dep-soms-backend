package com.dep.soms.dto.shift;


import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckInRequest {
    @NotNull
    private Long shiftAssignmentId;

    private String location;

    private Double latitude;

    private Double longitude;

    private String notes;
}
