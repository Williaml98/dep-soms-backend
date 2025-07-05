package com.dep.soms.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {
    @Size(max = 50)
    @Email
    private String email;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String preferredLanguage;

    private String currentPassword;

    @Size(min = 6, max = 40)
    private String newPassword;
}
