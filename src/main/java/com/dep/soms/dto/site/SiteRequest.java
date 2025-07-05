package com.dep.soms.dto.site;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SiteRequest {

    @NotNull
    private Long clientId;

    @NotBlank
    private String name;

    @NotBlank
    private String siteCode;

    @NotBlank
    private String address;

    private String city;

    private String country;

    private String contactPerson;

    private String contactPhone;

    private Double latitude;

    private Double longitude;

    private boolean active = true;
}
