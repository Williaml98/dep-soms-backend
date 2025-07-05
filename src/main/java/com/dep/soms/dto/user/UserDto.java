package com.dep.soms.dto.user;

import com.dep.soms.model.Role;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Set<String> roles;
    private boolean active;
    private String profilePicture;
}
