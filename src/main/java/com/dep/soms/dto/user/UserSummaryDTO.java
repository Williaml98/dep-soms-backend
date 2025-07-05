package com.dep.soms.dto.user;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private boolean active;
    private LocalDateTime lastLogin;
    private String roles; // Comma-separated role names
}
