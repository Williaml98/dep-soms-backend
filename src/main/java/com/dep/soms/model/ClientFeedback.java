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
//@Table(name = "client_feedback")
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@EntityListeners(AuditingEntityListener.class)
//public class ClientFeedback {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "client_id", nullable = false)
//    private Client client;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "site_id")
//    private Site site;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "submitted_by", nullable = false)
//    private User submittedBy;
//
//    @Column(name = "rating", nullable = false)
//    private Integer rating;
//
//    @Column(length = 1000)
//    private String comments;
//
//    @Column(name = "service_quality_rating")
//    private Integer serviceQualityRating;
//
//    @Column(name = "response_time_rating")
//    private Integer responseTimeRating;
//
//    @Column(name = "professionalism_rating")
//    private Integer professionalismRating;
//
//    @Column(name = "communication_rating")
//    private Integer communicationRating;
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
@Table(name = "client_feedback")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ClientFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", nullable = false)
    private User submittedBy;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(length = 1000)
    private String comments;

    @Column(name = "service_quality_rating")
    private Integer serviceQualityRating;

    @Column(name = "response_time_rating")
    private Integer responseTimeRating;

    @Column(name = "professionalism_rating")
    private Integer professionalismRating;

    @Column(name = "communication_rating")
    private Integer communicationRating;

    @Column(name = "guard_rating")
    private Integer guardRating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guard_id")
    private Guard guard;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
