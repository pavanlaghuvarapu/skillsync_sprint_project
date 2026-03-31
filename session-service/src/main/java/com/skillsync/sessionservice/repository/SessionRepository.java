package com.skillsync.sessionservice.repository;

import com.skillsync.sessionservice.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByLearnerEmail(String learnerEmail);
    List<Session> findByMentorId(Long mentorId);
    List<Session> findByMentorEmail(String mentorEmail);
}
