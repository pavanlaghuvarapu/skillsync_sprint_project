package com.skillsync.sessionservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "mentor-service", fallback = MentorFeignFallback.class)
public interface MentorFeignClient {

    @GetMapping("/api/mentors/{id}")
    ResponseEntity<Object> getMentorById(@PathVariable Long id);
}
