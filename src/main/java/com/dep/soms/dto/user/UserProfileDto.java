package com.dep.soms.dto.user;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String profilePicture;
    private boolean active;
    private LocalDateTime lastLogin;
    private Set<String> roles;
    private String preferredLanguage;
}

