package com.dep.soms.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "patrol_assignments")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PatrolAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patrol_id", nullable = false)
    private Patrol patrol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id", nullable = false)
    private User supervisor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PatrolAssignmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "patrol_type")
    private Patrol.PatrolType patrolType;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "declined_at")
    private LocalDateTime declinedAt;

    @Column(name = "decline_reason")
    private String declineReason;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "notes", length = 2000)
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum PatrolAssignmentStatus {
        PENDING,
        ACCEPTED,
        DECLINED,
        COMPLETED
    }
}