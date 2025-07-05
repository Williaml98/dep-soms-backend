package com.dep.soms.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientFeedbackDto {
    private Long id;
    private Long clientId;
    private String clientName;
    private Long siteId;
    private String siteName;
    private Long submittedById;
    private String submittedByName;
    private Integer rating;
    private String comments;
    private Integer serviceQualityRating;
    private Integer responseTimeRating;
    private Integer professionalismRating;
    private Integer communicationRating;
    private Integer guardRating; // Added guard rating
    private Long guardId; // Added guard ID
    private String guardName; // Added guard name
    private LocalDateTime createdAt;
}

