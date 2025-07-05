package com.dep.soms.dto.guard;

import com.dep.soms.dto.user.UserDto;
import com.dep.soms.model.Guard;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuardDto {
    private Long id;
    private Long userId;
    private Long personRegistrationId;
    private UserDto user;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String badgeNumber;
    private String licenseNumber;
    private LocalDate licenseExpiry;
    private String address;
    private String emergencyContact;
    private String emergencyPhone;
    private Guard.GuardStatus status;
    private String profilePicture;
    private LocalDate hireDate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDto {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String profilePicture;
    }
}

