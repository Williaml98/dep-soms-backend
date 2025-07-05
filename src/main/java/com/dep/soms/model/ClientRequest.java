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
@Table(name = "client_requests")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ClientRequest {

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
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private RequestPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Column(name = "resolution_notes", length = 1000)
    private String resolutionNotes;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @OneToMany(mappedBy = "clientRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ClientRequestComment> comments = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum RequestPriority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }

    public enum RequestStatus {
        OPEN,
        ASSIGNED,
        IN_PROGRESS,
        RESOLVED,
        CLOSED,
        REOPENED
    }
}
