package com.skillsync.sessionservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String learnerEmail;

    @Column(nullable = false)
    private Long mentorId;

    private String mentorEmail;
    private String topic;
    private String description;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    private Integer durationMinutes = 60;

    @Enumerated(EnumType.STRING)
    private SessionStatus status = SessionStatus.REQUESTED;

    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum SessionStatus {
        REQUESTED, ACCEPTED, REJECTED, COMPLETED, CANCELLED
    }
}
