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
//@Table(name = "incidents")
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@EntityListeners(AuditingEntityListener.class)
//public class Incident {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "site_id", nullable = false)
//    private Site site;
//
////    @ManyToOne(fetch = FetchType.LAZY)
////    @JoinColumn(name = "reported_by", nullable = false)
////    private User reportedBy;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "reported_by_user")
//    private User reportedByUser;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "guard_id")
//    private Guard guard;
//
//
//    @Column(name = "incident_time", nullable = false)
//    private LocalDateTime incidentTime;
//
//    @Column(nullable = false)
//    private String title;
//
//    @Column(length = 2000)
//    private String description;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "severity")
//    private IncidentSeverity severity;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "status")
//    private IncidentStatus status;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "incident_type")
//    private IncidentType incidentType;
//
//
//    @Column(name = "location_details")
//    private String locationDetails;
//
//    @Column(name = "latitude")
//    private Double latitude;
//
//    @Column(name = "longitude")
//    private Double longitude;
//
//    @Column(name = "image")
//    private String image;
//
//    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<IncidentAttachment> attachments = new HashSet<>();
//
//    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<IncidentComment> comments = new HashSet<>();
//
//    @CreatedDate
//    @Column(name = "created_at", nullable = false, updatable = false)
//    private LocalDateTime createdAt;
//
//    @LastModifiedDate
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;
//
//    public enum IncidentSeverity {
//        LOW,
//        MEDIUM,
//        HIGH,
//        CRITICAL
//    }
//
//
//    public enum IncidentStatus {
//        REPORTED,
//        UNDER_INVESTIGATION,
//        RESOLVED,
//        CLOSED,
//        REOPENED
//    }
//    public enum IncidentType {
//        THEFT, TRESPASSING, VANDALISM, OTHER
//    }
//
//}
//


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
@Table(name = "incidents")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by_user")
    private User reportedByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guard_id")
    private Guard guard;

    // NEW: Link to the specific shift assignment when incident occurred
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_assignment_id")
    private ShiftAssignment shiftAssignment;

    @Column(name = "incident_time", nullable = false)
    private LocalDateTime incidentTime;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity")
    private IncidentSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private IncidentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "incident_type")
    private IncidentType incidentType;

    @Column(name = "location_details")
    private String locationDetails;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    // Removed single image field - now using proper attachments
    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<IncidentAttachment> attachments = new HashSet<>();

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<IncidentComment> comments = new HashSet<>();

    // Optional fields
    @Column(name = "action_taken", length = 1000)
    private String actionTaken;

    @Column(name = "involved_parties", length = 1000)
    private String involvedParties;

    @Column(name = "reported_to")
    private String reportedTo;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum IncidentSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum IncidentStatus {
        REPORTED, UNDER_INVESTIGATION, RESOLVED, CLOSED, REOPENED
    }

    public enum IncidentType {
        THEFT, TRESPASSING, VANDALISM, FIRE, MEDICAL_EMERGENCY, SUSPICIOUS_ACTIVITY, EQUIPMENT_FAILURE, OTHER
    }
}
