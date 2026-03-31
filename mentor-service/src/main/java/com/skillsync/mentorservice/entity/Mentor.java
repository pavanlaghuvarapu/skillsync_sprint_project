package com.skillsync.mentorservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "mentors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mentor implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String fullName;
    private String bio;
    private Integer experienceYears;
    private Double pricePerHour;
    private Double averageRating = 0.0;
    private Integer totalReviews = 0;
    private String availability;

    @Enumerated(EnumType.STRING)
    private MentorStatus status = MentorStatus.PENDING;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "mentor_skills",
        joinColumns = @JoinColumn(name = "mentor_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private List<Skill> skills;

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

    public enum MentorStatus {
        PENDING, APPROVED, REJECTED
    }
}
