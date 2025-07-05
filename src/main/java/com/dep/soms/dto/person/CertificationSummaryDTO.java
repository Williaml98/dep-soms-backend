package com.dep.soms.dto.person;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificationSummaryDTO {
    private Long id;
    private String name;
    private String description;
    private String issuingAuthority;
    private Integer validityPeriodMonths;
    private boolean required;
}
