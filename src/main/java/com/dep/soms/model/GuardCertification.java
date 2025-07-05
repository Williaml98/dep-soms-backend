package com.dep.soms.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Getter
@Setter
@Entity
@Table(name = "guard_certifications")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class GuardCertification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guard_id", nullable = false)
    private Guard guard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_id", nullable = false)
    private Certification certification;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "certificate_number")
    private String certificateNumber;

    @Column(name = "certificate_file")
    private String certificateFile;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

