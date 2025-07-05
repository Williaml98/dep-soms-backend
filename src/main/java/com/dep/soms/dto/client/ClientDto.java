package com.dep.soms.dto.client;


import com.dep.soms.dto.site.SiteDto;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDto {
    private Long id;
    private Long userId;
    @NotBlank
    private String name;
    private String companyName;
    private String address;
    private String contactPerson;
    private String contactEmail;
    private String contactPhone;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private String contractNumber;
    private String city;
    private String industry;
    private String notes;
    private String country;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<SiteDto> sites; // Optional, only if you want to include sites
//    private Long id;
//    private Long userId;
//    private String companyName;
//    private String contactPerson;
//    private String contactEmail;
//    private String contactPhone;
//    private String address;
//    private String city;
//    private String state;
//    private String zipCode;
//    private String country;
//    private String industry;
//    private boolean active;
//    private String notes;
//    private String username;
//    private String firstname;
//    private String lastname;
}
