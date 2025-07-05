package com.dep.soms.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "shifts")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(nullable = false)
    private String name;

    // Added description field
    @Column(length = 500)
    private String description;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "required_guards")
    private Integer requiredGuards;

    @Column(name = "is_active")
    private boolean active;

    // Added shift type enum
    @Column(name = "shift_type")
    @Enumerated(EnumType.STRING)
    private ShiftType shiftType;

    // For recurring shifts - store as a string like "1,2,5" for Monday, Tuesday, Friday
    @Column(name = "days_of_week")
    private String daysOfWeek;

    // For one-time shifts or exceptions to recurring patterns
    @Column(name = "specific_date")
    private LocalDate specificDate;

    // Reference to a shift pattern for complex recurring schedules
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pattern_id")
    private ShiftPattern pattern;

    @Column(name = "break_duration_minutes")
    private Integer breakDurationMinutes;

    @Column(name = "notes", length = 500)
    private String notes;

    // Color code for UI display
    @Column(name = "color_code", length = 7)
    private String colorCode;

    // Skills required for this shift
//    @ManyToMany
//    @JoinTable(
//            name = "shift_required_skills",
//            joinColumns = @JoinColumn(name = "shift_id"),
//            inverseJoinColumns = @JoinColumn(name = "skill_id")
//    )
//    private Set<Skill> requiredSkills = new HashSet<>();

    @OneToMany(mappedBy = "shift", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ShiftAssignment> assignments = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    // Minimum rest time between shifts in hours
    @Column(name = "minimum_rest_hours")
    private Integer minimumRestHours;

    public enum ShiftType {
        MORNING,
        EVENING,
        WEEKEND,
        HOLIDAY,
        CUSTOM,
        MONTHLY
    }
}
