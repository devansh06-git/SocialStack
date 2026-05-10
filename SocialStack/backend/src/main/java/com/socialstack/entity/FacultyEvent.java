package com.socialstack.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "faculty_events",
    uniqueConstraints = @UniqueConstraint(columnNames = {"faculty_id", "event_id"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FacultyEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    private User faculty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(name = "faculty_role", nullable = false)
    private FacultyRole facultyRole;

    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    public enum FacultyRole {
        HEAD, COORDINATOR
    }
}
