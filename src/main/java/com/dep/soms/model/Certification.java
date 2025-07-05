//package com.dep.soms.model;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//@Getter
//@Setter
//@Entity
//@Table(name = "certifications")
//@NoArgsConstructor
//@AllArgsConstructor
//public class Certification {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false, unique = true)
//    private String name;
//
//    @Column(length = 500)
//    private String description;
//
//    @Column(name = "issuing_authority")
//    private String issuingAuthority;
//
//    @Column(name = "validity_period_months")
//    private Integer validityPeriodMonths;
//
//    @Column(name = "is_required")
//    private boolean required;
//}

package com.dep.soms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "certifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    private String issuingAuthority;

    private String code;

    private Integer validityPeriodMonths;

    @ManyToMany(mappedBy = "certifications")
    private Set<TrainingSession> trainingSessions = new HashSet<>();

    @OneToMany(mappedBy = "certification")
    private Set<TrainingCertification> trainingCertifications = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
