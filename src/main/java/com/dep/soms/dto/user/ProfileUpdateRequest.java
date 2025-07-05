package com.dep.soms.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateRequest {
    @Size(max = 50)
    @Email
    private String email;

    private String firstName;

    private String lastName;

    private String phoneNumber;
}

