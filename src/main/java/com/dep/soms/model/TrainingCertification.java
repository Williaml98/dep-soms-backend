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
@Table(name = "training_certifications")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TrainingCertification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_record_id", nullable = false)
    private TrainingRecord trainingRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_id", nullable = false)
    private Certification certification;

    @Column(name = "issued_date", nullable = false)
    private LocalDate issuedDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "certificate_number", unique = true)
    private String certificateNumber;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
