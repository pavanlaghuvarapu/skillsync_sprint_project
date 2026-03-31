package com.skillsync.reviewservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "mentor-service", fallback = MentorFeignFallback.class)
public interface MentorFeignClient {

    @PutMapping("/api/mentors/{id}/rating")
    ResponseEntity<Void> updateMentorRating(@PathVariable Long id, @RequestParam Double rating);
}
