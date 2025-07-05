package com.dep.soms.dto.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientUpdateRequest {
    @Size(max = 50)
    @Email
    private String email;

    private String companyName;

    private String contactPerson;

    private String phoneNumber;

    private String address;

    private String city;

    private String state;

    private String zipCode;

    private String country;

    private String industry;

    private boolean active;

    private String notes;
}
