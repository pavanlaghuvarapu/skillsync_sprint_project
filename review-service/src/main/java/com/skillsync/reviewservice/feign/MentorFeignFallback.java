package com.skillsync.reviewservice.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MentorFeignFallback implements MentorFeignClient {

    @Override
    public ResponseEntity<Void> updateMentorRating(Long id, Double rating) {
        log.warn("Mentor service DOWN! Rating update skipped for mentorId: {}", id);
        return ResponseEntity.status(503).build();
    }
}
