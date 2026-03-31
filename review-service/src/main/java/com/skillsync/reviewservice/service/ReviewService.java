package com.skillsync.reviewservice.service;

import com.skillsync.reviewservice.dto.ReviewRequest;
import com.skillsync.reviewservice.entity.Review;
import com.skillsync.reviewservice.feign.MentorFeignClient;
import com.skillsync.reviewservice.repository.ReviewRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MentorFeignClient mentorFeignClient;

    @CircuitBreaker(name = "mentorService", fallbackMethod = "submitReviewFallback")
    public Review submitReview(String learnerEmail, ReviewRequest request) {
        Review review = new Review();
        review.setMentorId(request.getMentorId());
        review.setLearnerEmail(learnerEmail);
        review.setSessionId(request.getSessionId());
        review.setRating(request.getRating());
        review.setReviewText(request.getReviewText());
        Review saved = reviewRepository.save(review);

        // Feign - auto update mentor average rating
        try {
            mentorFeignClient.updateMentorRating(request.getMentorId(), (double) request.getRating());
            log.info("Mentor rating updated via Feign for mentorId: {}", request.getMentorId());
        } catch (Exception e) {
            log.error("Failed to update mentor rating: {}", e.getMessage());
        }
        return saved;
    }

    // Circuit breaker fallback
    public Review submitReviewFallback(String learnerEmail, ReviewRequest request, Exception ex) {
        log.error("Circuit breaker triggered for review submission: {}", ex.getMessage());
        throw new RuntimeException("Mentor service unavailable. Review saved but rating not updated.");
    }

    public List<Review> getByMentor(Long mentorId) {
        return reviewRepository.findByMentorId(mentorId);
    }

    public List<Review> getByLearner(String email) {
        return reviewRepository.findByLearnerEmail(email);
    }

    public Double getAverageRating(Long mentorId) {
        Double avg = reviewRepository.findAverageRating(mentorId);
        return avg != null ? avg : 0.0;
    }

    public Review getById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found: " + id));
    }
}
