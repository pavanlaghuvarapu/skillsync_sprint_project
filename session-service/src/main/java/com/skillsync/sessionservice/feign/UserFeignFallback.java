package com.skillsync.sessionservice.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserFeignFallback implements UserFeignClient {

    @Override
    public ResponseEntity<Object> getUserByEmail(String email) {
        log.warn("User service is DOWN! Fallback triggered for email: {}", email);
        return ResponseEntity.status(503).body(
            "User service is currently unavailable. Please try again later."
        );
    }
}
