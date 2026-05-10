package com.socialstack.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_name", nullable = false, length = 200)
    private String eventName;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(name = "event_time")
    private LocalTime eventTime;

    @Column(length = 200)
    private String location;

    @Column(length = 80)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 120)
    private String organizer;

    @Column(length = 180)
    private String email;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by")
    private User submittedBy;

    /** Faculty to assign as HEAD/COORDINATOR only after the event is approved. */
    @Column(name = "requested_faculty_id")
    private Long requestedFacultyId;

    @Column(name = "requested_faculty_role", length = 20)
    private String requestedFacultyRole;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Status {
        PENDING, APPROVED, REJECTED
    }
}
