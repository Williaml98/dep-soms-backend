

package com.dep.soms.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "shift_assignments")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ShiftAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guard_id", nullable = false)
    private Guard guard;

    @Column(name = "assignment_date", nullable = false)
    private LocalDate assignmentDate;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AssignmentStatus status;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "check_in_latitude")
    private Double checkInLatitude;

    @Column(name = "check_in_longitude")
    private Double checkInLongitude;

    @Column(name = "check_out_latitude")
    private Double checkOutLatitude;

    @Column(name = "check_out_longitude")
    private Double checkOutLongitude;

    // New fields for storing check-in/out messages and location verification
    @Column(name = "check_in_message", length = 500)
    private String checkInMessage;

    @Column(name = "check_out_message", length = 500)
    private String checkOutMessage;

    @Column(name = "check_in_location_verified")
    private Boolean checkInLocationVerified;

    @Column(name = "check_out_location_verified")
    private Boolean checkOutLocationVerified;

    @Column(name = "check_in_distance_meters")
    private Double checkInDistanceMeters;

    @Column(name = "check_out_distance_meters")
    private Double checkOutDistanceMeters;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum AssignmentStatus {
        SCHEDULED,
        IN_PROGRESS,
        COMPLETED,
        MISSED,
        REASSIGNED,
        ASSIGNED,
        CHECKED_IN,
        ABSENT,
        INCOMPLETE
    }
}

