//package com.dep.soms.model;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import jakarta.persistence.*;
//import lombok.*;
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.annotation.LastModifiedDate;
//import org.springframework.data.jpa.domain.support.AuditingEntityListener;
//
//import java.time.LocalDateTime;
//import java.util.HashSet;
//import java.util.Set;
//
//@Getter
//@Setter
//@Entity
//@Table(name = "training_sessions")
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@EntityListeners(AuditingEntityListener.class)
//public class TrainingSession {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false)
//    private String title;
//
//    @Column(length = 1000)
//    private String description;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "trainer_id")
//    private User trainer;
//
//    @Column(name = "start_time", nullable = false)
//    private LocalDateTime startTime;
//
//    @Column(name = "end_time", nullable = false)
//    private LocalDateTime endTime;
//
//    @Column(name = "location")
//    private String location;
//
//    @Column(name = "max_participants")
//    private Integer maxParticipants;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "status")
//    private TrainingStatus status;
//
//    @JsonIgnore
//    @ManyToMany
//    @JoinTable(
//            name = "training_session_certifications",
//            joinColumns = @JoinColumn(name = "training_session_id"),
//            inverseJoinColumns = @JoinColumn(name = "certification_id")
//    )
//    @Builder.Default
//    private Set<Certification> certifications = new HashSet<>();
//
//    // Updated to properly handle cascade deletion
//    @JsonIgnore
//    @OneToMany(
//            mappedBy = "trainingSession",
//            cascade = CascadeType.ALL,
//            orphanRemoval = true,
//            fetch = FetchType.LAZY
//    )
//    @Builder.Default
//    private Set<TrainingAttendance> attendances = new HashSet<>();
//
//    @CreatedDate
//    @Column(name = "created_at", nullable = false, updatable = false)
//    private LocalDateTime createdAt;
//
//    @LastModifiedDate
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;
//
//    // Helper method to properly handle bidirectional relationship
//    public void addAttendance(TrainingAttendance attendance) {
//        attendances.add(attendance);
//        attendance.setTrainingSession(this);
//    }
//
//    public void removeAttendance(TrainingAttendance attendance) {
//        attendances.remove(attendance);
//        attendance.setTrainingSession(null);
//    }
//
//    public enum TrainingStatus {
//        SCHEDULED,
//        IN_PROGRESS,
//        COMPLETED,
//        CANCELLED
//    }
//}

package com.dep.soms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "training_sessions")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TrainingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id")
    private User trainer;

    // Start date of the training session (first day)
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    // End date of the training session (last day)
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // Daily start time for the training
    @Column(name = "daily_start_time", nullable = false)
    private LocalTime dailyStartTime;

    // Daily end time for the training
    @Column(name = "daily_end_time", nullable = false)
    private LocalTime dailyEndTime;

    // For backward compatibility - will be derived from start_date and daily_start_time
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    // For backward compatibility - will be derived from end_date and daily_end_time
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "location")
    private String location;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TrainingStatus status;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "training_session_certifications",
            joinColumns = @JoinColumn(name = "training_session_id"),
            inverseJoinColumns = @JoinColumn(name = "certification_id")
    )
    @Builder.Default
    private Set<Certification> certifications = new HashSet<>();

    @JsonIgnore
    @OneToMany(
            mappedBy = "trainingSession",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<TrainingAttendance> attendances = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper method to properly handle bidirectional relationship
    public void addAttendance(TrainingAttendance attendance) {
        attendances.add(attendance);
        attendance.setTrainingSession(this);
    }

    public void removeAttendance(TrainingAttendance attendance) {
        attendances.remove(attendance);
        attendance.setTrainingSession(null);
    }

    // Helper method to calculate the number of days in the training session
    public int getDurationInDays() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    public enum TrainingStatus {
        SCHEDULED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }
}
