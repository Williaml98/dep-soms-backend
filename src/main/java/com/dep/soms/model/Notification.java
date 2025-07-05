package com.dep.soms.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
@Getter
@Setter
@Entity
@Table(name = "notifications")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private NotificationType type;

    @Column(name = "is_read")
    private boolean read;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "link")
    private String link;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum NotificationType {

        INCIDENT,
        SHIFT_ASSIGNMENT,
        CLIENT_REQUEST,
        SYSTEM,
        PATROL_ALERT,
        TRAINING,
        MESSAGE,
        REPORT
    }
}
