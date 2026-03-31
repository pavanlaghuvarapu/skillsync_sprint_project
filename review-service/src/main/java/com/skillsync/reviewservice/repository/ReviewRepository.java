package com.skillsync.reviewservice.repository;

import com.skillsync.reviewservice.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByMentorId(Long mentorId);
    List<Review> findByLearnerEmail(String learnerEmail);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.mentorId = :mentorId")
    Double findAverageRating(@Param("mentorId") Long mentorId);
}
