//package com.dep.soms.model;
//
//import jakarta.persistence.*;
//import lombok.*;
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.annotation.LastModifiedDate;
//import org.springframework.data.jpa.domain.support.AuditingEntityListener;
//
//import java.time.LocalDateTime;
//
//@Getter
//@Setter
//@Entity
//@Table(name = "training_attendances")
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@EntityListeners(AuditingEntityListener.class)
//public class TrainingAttendance {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "training_session_id", nullable = false)
//    private TrainingSession trainingSession;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "person_registration_id", nullable = false)
//    private PersonRegistration personRegistration;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "training_record_id", nullable = false)
//    private TrainingRecord trainingRecord;
//
//    @Column(name = "registered_at")
//    private LocalDateTime registeredAt;
//
//    @Column(name = "attended")
//    private Boolean attended;
//
//    @Column(name = "check_in_time")
//    private LocalDateTime checkInTime;
//
//    @Column(name = "check_out_time")
//    private LocalDateTime checkOutTime;
//
//    @Column(name = "passed")
//    private Boolean passed;
//
//    @Column(name = "score")
//    private Integer score;
//
//    @Column(name = "notes", length = 500)
//    private String notes;
//
//    @CreatedDate
//    @Column(name = "created_at", nullable = false, updatable = false)
//    private LocalDateTime createdAt;
//
//    @LastModifiedDate
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;
//}

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
@Table(name = "training_attendances")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TrainingAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_session_id", nullable = false)
    private TrainingSession trainingSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_registration_id", nullable = false)
    private PersonRegistration personRegistration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_record_id", nullable = false)
    private TrainingRecord trainingRecord;

    // The specific date this attendance record is for
    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @Column(name = "attended")
    private Boolean attended;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "score")
    private Integer score;

    @Column(name = "passed")
    private Boolean passed;

    @Column(name = "notes", length = 1000)
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
