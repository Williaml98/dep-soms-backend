//package com.dep.soms.model;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.annotation.LastModifiedDate;
//import org.springframework.data.jpa.domain.support.AuditingEntityListener;
//
//import java.time.LocalDateTime;
//import java.util.HashSet;
//import java.util.Set;
//
//@Entity
//@Table(name = "sites")
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@EntityListeners(AuditingEntityListener.class)
//public class Site {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "client_id", nullable = false)
//    private Client client;
//
//    @Column(nullable = false)
//    private String name;
//
//    @Column(length = 500)
//    private String address;
//
//    @Column(name = "site_code", unique = true)
//    private String siteCode;
//
//    @Column(name = "contact_person")
//    private String contactPerson;
//
//    @Column(name = "contact_phone")
//    private String contactPhone;
//
//    @Column(name = "latitude")
//    private Double latitude;
//
//    @Column(name = "longitude")
//    private Double longitude;
//
//    @Column(name = "is_active")
//    private boolean active;
//
//    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<PatrolPoint> patrolPoints = new HashSet<>();
//
//    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<Shift> shifts = new HashSet<>();
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

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
@Getter
@Setter
@Entity
@Table(name = "sites")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;

    @Column(name = "site_code", unique = true)
    private String siteCode;

    @Column(name = "contact_person")
    private String contactPerson;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "is_active")
    private boolean active;

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PatrolPoint> patrolPoints = new HashSet<>();

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Shift> shifts = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
