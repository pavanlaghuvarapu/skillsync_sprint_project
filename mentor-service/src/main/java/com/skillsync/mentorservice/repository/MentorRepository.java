package com.skillsync.mentorservice.repository;

import com.skillsync.mentorservice.entity.Mentor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface MentorRepository extends JpaRepository<Mentor, Long> {
    Optional<Mentor> findByEmail(String email);
    List<Mentor> findByStatus(Mentor.MentorStatus status);

    @Query("SELECT m FROM Mentor m JOIN m.skills s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%',:skill,'%')) AND m.status='APPROVED'")
    List<Mentor> findBySkillName(@Param("skill") String skill);

    @Query("SELECT m FROM Mentor m WHERE m.status='APPROVED' AND m.averageRating>=:minRating AND m.experienceYears>=:minExp AND m.pricePerHour<=:maxPrice")
    List<Mentor> findByFilters(@Param("minRating") Double minRating,
                               @Param("minExp") Integer minExp,
                               @Param("maxPrice") Double maxPrice);
}
