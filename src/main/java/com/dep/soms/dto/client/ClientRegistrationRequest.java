package com.dep.soms.dto.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ClientRegistrationRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    @NotBlank
    private String companyName;

    @NotBlank
    private String contactPerson;

    private String contactEmail;

    private String contactPhone;

    private String contractNumber;

    private LocalDate contractStartDate;

    private LocalDate contractEndDate;

    private String phoneNumber;

    private String address;

    private String city;

    private String country;

    private String industry;

    private String notes;

    private String name;

    private String firstname;

    private String lastname;
}
