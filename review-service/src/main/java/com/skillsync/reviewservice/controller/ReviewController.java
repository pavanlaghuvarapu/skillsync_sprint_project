package com.skillsync.reviewservice.controller;

import com.skillsync.reviewservice.dto.ReviewRequest;
import com.skillsync.reviewservice.entity.Review;
import com.skillsync.reviewservice.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Submit and retrieve mentor reviews and ratings")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Submit review - LEARNER only", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    @PreAuthorize("hasRole('ROLE_LEARNER')")
    public ResponseEntity<Review> submit(Authentication auth, @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.submitReview(auth.getName(), request));
    }

    @Operation(summary = "Get reviews for a mentor (public)")
    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<List<Review>> getByMentor(@PathVariable Long mentorId) {
        return ResponseEntity.ok(reviewService.getByMentor(mentorId));
    }

    @Operation(summary = "Get average rating for a mentor (public)")
    @GetMapping("/mentor/{mentorId}/average")
    public ResponseEntity<Double> getAverage(@PathVariable Long mentorId) {
        return ResponseEntity.ok(reviewService.getAverageRating(mentorId));
    }

    @Operation(summary = "Get my reviews", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Review>> getMyReviews(Authentication auth) {
        return ResponseEntity.ok(reviewService.getByLearner(auth.getName()));
    }

    @Operation(summary = "Get review by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Review> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getById(id));
    }
}
