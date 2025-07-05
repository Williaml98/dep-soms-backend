package com.dep.soms.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "training_records")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TrainingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_registration_id", nullable = false)
    private PersonRegistration personRegistration;

    @Column(name = "enrolled_at")
    private LocalDateTime enrolledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "overall_score")
    private Integer overallScore;

    @Column(name = "passed")
    private Boolean passed;

    @Enumerated(EnumType.STRING)
    @Column(name = "training_status")
    private TrainingStatus trainingStatus;

    // Training sessions attended
    @OneToMany(mappedBy = "trainingRecord", cascade = CascadeType.ALL)
    private Set<TrainingAttendance> attendances = new HashSet<>();

    // Certifications earned (only if passed)
    @OneToMany(mappedBy = "trainingRecord", cascade = CascadeType.ALL)
    private Set<TrainingCertification> certifications = new HashSet<>();

    @Column(name = "trainer_notes", length = 1000)
    private String trainerNotes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum TrainingStatus {
        ENROLLED,
        IN_PROGRESS,
        COMPLETED_PASSED,
        COMPLETED_FAILED,
        DROPPED_OUT
    }
}
