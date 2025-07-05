package com.dep.soms.dto.guard;

import com.dep.soms.model.Guard;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class GuardRegistrationRequest {
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
    private String firstName;

    @NotBlank
    private String lastName;

    private String phoneNumber;

    @NotBlank
    private String badgeNumber;

    private String licenseNumber;

    private LocalDate licenseExpiry;

    private String address;

    private String emergencyContact;

    private String emergencyPhone;

    @NotNull
    private Guard.GuardStatus status;
}

