package com.dep.soms.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "shift_patterns")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ShiftPattern {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    // Stores JSON or serialized representation of the pattern
    @Column(name = "pattern_definition", columnDefinition = "TEXT")
    private String patternDefinition;

    // e.g., "WEEKLY", "BIWEEKLY", "MONTHLY", "CUSTOM"
    @Column(name = "pattern_type")
    @Enumerated(EnumType.STRING)
    private PatternType patternType;

    // Rotation length in days (e.g., 7 for weekly, 14 for biweekly)
    @Column(name = "rotation_length")
    private Integer rotationLength;

    @Column(name = "is_active")
    private boolean active;

    @OneToMany(mappedBy = "pattern")
    private Set<Shift> shifts = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum PatternType {
        WEEKLY,
        BIWEEKLY,
        MONTHLY,
        CUSTOM
    }
}
