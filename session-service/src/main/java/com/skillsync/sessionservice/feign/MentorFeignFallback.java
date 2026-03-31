package com.skillsync.sessionservice.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MentorFeignFallback implements MentorFeignClient {

    @Override
    public ResponseEntity<Object> getMentorById(Long id) {
        log.warn("Mentor service is DOWN! Fallback triggered for mentorId: {}", id);
        return ResponseEntity.status(503).body(
            "Mentor service is currently unavailable. Please try again later."
        );
    }
}
