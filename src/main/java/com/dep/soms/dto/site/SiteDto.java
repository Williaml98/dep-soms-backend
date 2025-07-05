package com.dep.soms.dto.site;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteDto {
    private Long id;
    private Long clientId;
    private String clientName;
    private String name;
    private String siteCode;
    private String address;
    private String city;
    private String country;
    private String contactPerson;
    private String contactPhone;
    private Double latitude;
    private Double longitude;
    private boolean active;
}
