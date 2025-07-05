package com.dep.soms.dto.guard;

import com.dep.soms.model.Guard;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class GuardUpdateRequest {
    @Size(max = 50)
    @Email
    private String email;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String badgeNumber;

    private String licenseNumber;

    private LocalDate licenseExpiry;

    private String address;

    private String emergencyContact;

    private String emergencyPhone;

    private Guard.GuardStatus status;
}
