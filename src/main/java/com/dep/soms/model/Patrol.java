//package com.dep.soms.model;
//
//import jakarta.persistence.*;
//import lombok.*;
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.annotation.LastModifiedDate;
//import org.springframework.data.jpa.domain.support.AuditingEntityListener;
//
//import java.time.LocalDateTime;
//import java.util.HashSet;
//import java.util.Set;
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
//    @JoinColumn(name = "guard_id", nullable = false)
//    private Guard guard;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id", nullable = false)
    private User supervisor;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PatrolStatus status;

    @Column(name = "notes", length = 500)
    private String notes;

    @OneToMany(mappedBy = "patrol", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PatrolCheckpoint> checkpoints = new HashSet<>();

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
        INCOMPLETE
    }
}