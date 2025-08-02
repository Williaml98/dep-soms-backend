//package com.dep.soms.model;
//
//import jakarta.persistence.*;
//import lombok.*;
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.jpa.domain.support.AuditingEntityListener;
//
//import java.time.LocalDateTime;
//@Getter
//@Setter
//@Entity
//@Table(name = "patrol_checkpoints")
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@EntityListeners(AuditingEntityListener.class)
//public class PatrolCheckpoint {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "patrol_id", nullable = false)
//    private Patrol patrol;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "patrol_point_id", nullable = false)
//    private PatrolPoint patrolPoint;
//
//    @Column(name = "check_time", nullable = false)
//    private LocalDateTime checkTime;
//
//    @Column(name = "latitude")
//    private Double latitude;
//
//    @Column(name = "longitude")
//    private Double longitude;
//
//    @Column(name = "notes", length = 500)
//    private String notes;
//
//    @Column(name = "photo_path")
//    private String photoPath;
//
//    @CreatedDate
//    @Column(name = "created_at", nullable = false, updatable = false)
//    private LocalDateTime createdAt;
//}

package com.dep.soms.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "patrol_checkpoints")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PatrolCheckpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patrol_id", nullable = false)
    private Patrol patrol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patrol_point_id", nullable = false)
    private PatrolPoint patrolPoint;

    // Remove nullable = false since checkpoints are created before patrol starts
    @Column(name = "check_time")
    private LocalDateTime checkTime;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "photo_path")
    private String photoPath;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}