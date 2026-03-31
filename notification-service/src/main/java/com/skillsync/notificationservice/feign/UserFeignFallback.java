package com.skillsync.notificationservice.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserFeignFallback implements UserFeignClient {

    @Override
    public ResponseEntity<Object> getUserByEmail(String email) {
        log.warn("User service DOWN! Proceeding with notification for: {}", email);
        return ResponseEntity.status(503).body("User service unavailable");
    }
}
