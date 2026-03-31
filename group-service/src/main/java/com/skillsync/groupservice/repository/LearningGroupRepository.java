package com.skillsync.groupservice.repository;

import com.skillsync.groupservice.entity.LearningGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LearningGroupRepository extends JpaRepository<LearningGroup, Long> {
    List<LearningGroup> findByCreatedByEmail(String email);
    List<LearningGroup> findByTopicContainingIgnoreCase(String topic);

    @Query("SELECT g FROM LearningGroup g WHERE :email MEMBER OF g.memberEmails")
    List<LearningGroup> findByMemberEmail(@Param("email") String email);
}
