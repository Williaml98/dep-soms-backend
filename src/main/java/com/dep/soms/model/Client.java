
package com.dep.soms.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "clients")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(name = "name", nullable = true)
    private String name;

    @Column(name = "company_name")
    private String companyName;

    @Column(length = 500)
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;

    @Column(name = "industry")
    private String industry;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "contact_person")
    private String contactPerson;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "contract_start_date")
    private LocalDate contractStartDate;

    @Column(name = "contract_end_date")
    private LocalDate contractEndDate;

    @Column(name = "contract_number")
    private String contractNumber;

    @Column(name = "is_active")
    private boolean active;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Site> sites = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
