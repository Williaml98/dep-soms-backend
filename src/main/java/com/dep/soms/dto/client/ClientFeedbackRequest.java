package com.dep.soms.dto.client;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientFeedbackRequest {
    @NotNull(message = "Client ID is required")
    private Long clientId;

    private Long siteId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    private String comments;

    @Min(value = 1, message = "Service quality rating must be between 1 and 5")
    @Max(value = 5, message = "Service quality rating must be between 1 and 5")
    private Integer serviceQualityRating;

    @Min(value = 1, message = "Response time rating must be between 1 and 5")
    @Max(value = 5, message = "Response time rating must be between 1 and 5")
    private Integer responseTimeRating;

    @Min(value = 1, message = "Professionalism rating must be between 1 and 5")
    @Max(value = 5, message = "Professionalism rating must be between 1 and 5")
    private Integer professionalismRating;

    @Min(value = 1, message = "Communication rating must be between 1 and 5")
    @Max(value = 5, message = "Communication rating must be between 1 and 5")
    private Integer communicationRating;

    @Min(value = 1, message = "Guard rating must be between 1 and 5")
    @Max(value = 5, message = "Guard rating must be between 1 and 5")
    private Integer guardRating;

    private Long guardId;
}

