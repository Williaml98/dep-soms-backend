package com.dep.soms.dto.patrol;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckpointUpdateRequest {
    private Double latitude;
    private Double longitude;
    private Double gpsAccuracy;
    private String notes;
    private String photoPath;
}