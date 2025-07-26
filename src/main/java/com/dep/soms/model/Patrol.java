////package com.dep.soms.model;
////
////import jakarta.persistence.*;
////import lombok.*;
////import org.springframework.data.annotation.CreatedDate;
////import org.springframework.data.annotation.LastModifiedDate;
////import org.springframework.data.jpa.domain.support.AuditingEntityListener;
////
////import java.time.LocalDateTime;
////import java.util.HashSet;
////import java.util.Set;
////@Getter
////@Setter
////@Entity
////@Table(name = "patrols")
////@Builder
////@NoArgsConstructor
////@AllArgsConstructor
////@EntityListeners(AuditingEntityListener.class)
////public class Patrol {
////
////    @Id
////    @GeneratedValue(strategy = GenerationType.IDENTITY)
////    private Long id;
////
////    @ManyToOne(fetch = FetchType.LAZY)
////    @JoinColumn(name = "site_id", nullable = false)
////    private Site site;
////
////    @ManyToOne(fetch = FetchType.LAZY)
////    @JoinColumn(name = "guard_id", nullable = false)
////    private Guard guard;
////
////    @Column(name = "start_time")
////    private LocalDateTime startTime;
////
////    @Column(name = "end_time")
////    private LocalDateTime endTime;
////
////    @Enumerated(EnumType.STRING)
////    @Column(name = "status")
////    private PatrolStatus status;
////
////    @Column(name = "notes", length = 500)
////    private String notes;
////
////    @OneToMany(mappedBy = "patrol", cascade = CascadeType.ALL, orphanRemoval = true)
////    private Set<PatrolCheckpoint> checkpoints = new HashSet<>();
////
////    @CreatedDate
////    @Column(name = "created_at", nullable = false, updatable = false)
////    private LocalDateTime createdAt;
////
////    @LastModifiedDate
////    @Column(name = "updated_at")
////    private LocalDateTime updatedAt;
////
////    public enum PatrolStatus {
////        SCHEDULED,
////        IN_PROGRESS,
////        COMPLETED,
////        MISSED,
////        INCOMPLETE
////    }
////}
//
//
//package com.dep.soms.model;
//
//import jakarta.persistence.*;
//        import lombok.*;
//        import org.springframework.data.annotation.CreatedDate;
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
//@Table(name = "patrols")
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@EntityListeners(AuditingEntityListener.class)
//public class Patrol {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "site_id", nullable = false)
//    private Site site;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "supervisor_id", nullable = false)
//    private User supervisor;
//
//    @Column(name = "start_time")
//    private LocalDateTime startTime;
//
//    @Column(name = "end_time")
//    private LocalDateTime endTime;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "status")
//    private PatrolStatus status;
//
//    @Column(name = "notes", length = 500)
//    private String notes;
//
//    @OneToMany(mappedBy = "patrol", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<PatrolCheckpoint> checkpoints = new HashSet<>();
//
//    @CreatedDate
//    @Column(name = "created_at", nullable = false, updatable = false)
//    private LocalDateTime createdAt;
//
//    @LastModifiedDate
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;
//
//    public enum PatrolStatus {
//        SCHEDULED,
//        IN_PROGRESS,
//        COMPLETED,
//        MISSED,
//        INCOMPLETE
//    }
//}


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
@Table(name = "patrols")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Patrol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_site_id", nullable = false)
    private Site primarySite;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "patrol_sites",
            joinColumns = @JoinColumn(name = "patrol_id"),
            inverseJoinColumns = @JoinColumn(name = "site_id")
    )
    private Set<Site> sites = new HashSet<>();

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "supervisor_id")
//    private User supervisor;

    // In Patrol.java
    @OneToMany(mappedBy = "patrol", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PatrolAssignment> assignments = new HashSet<>();


    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "patrol_type")
    private PatrolType patrolType;

    @Column(name = "required_supervisors")
    private Integer requiredSupervisors;

    @Column(name = "color_code")
    private String colorCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PatrolStatus status;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "active")
    private Boolean active;

    @OneToMany(mappedBy = "patrol", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PatrolCheckpoint> checkpoints = new HashSet<>();

    // Add these new fields to your Patrol entity class
    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime;

    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum PatrolStatus {
        SCHEDULED,
        IN_PROGRESS,
        COMPLETED,
        MISSED,
        INCOMPLETE,
        CANCELLED
    }

    public enum PatrolType {
        MORNING,
        AFTERNOON,
        EVENING,
        NIGHT,
        WEEKEND,
        CUSTOM
    }
}