package com.dep.soms.dto.client;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackSummaryDto {
    private Double overallRating;
    private Double serviceQualityRating;
    private Double responseTimeRating;
    private Double professionalismRating;
    private Double communicationRating;
    private Double guardRating;
    private Long totalFeedbackCount;
}
