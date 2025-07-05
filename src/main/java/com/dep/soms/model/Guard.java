package com.dep.soms.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
@Getter
@Setter
@Entity
@Table(name = "guards")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Guard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(name = "badge_number", unique = true)
    private String badgeNumber;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    @Column(name = "emergency_phone")
    private String emergencyPhone;

    @Column(name = "is_armed")
    private boolean armed;

    @Column(name = "license_number")
    private String licenseNumber;

    @Column(name = "license_expiry")
    private LocalDate licenseExpiry;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "guard_skills",
            joinColumns = @JoinColumn(name = "guard_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills = new HashSet<>();

    @OneToMany(mappedBy = "guard", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GuardCertification> certifications = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private GuardStatus status;

    // Add this relationship to your existing Guard entity
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_registration_id", nullable = false)
    private PersonRegistration personRegistration;


    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum GuardStatus {
        ACTIVE,
        ON_LEAVE,
        SUSPENDED,
        INACTIVE,
        TRAINING
    }
}
