package com.dep.soms.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "roles")
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, unique = true)
    private ERole name;

    public enum ERole {
        ROLE_ADMIN,
        ROLE_MANAGER,
        ROLE_GUARD,
        ROLE_CLIENT,
        ROLE_SUPERVISOR,
        ROLE_HR,
    }
}
