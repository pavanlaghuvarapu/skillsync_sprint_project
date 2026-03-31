package com.skillsync.sessionservice.service;

import com.skillsync.sessionservice.dto.SessionDtos;
import com.skillsync.sessionservice.entity.Session;
import com.skillsync.sessionservice.feign.MentorFeignClient;
import com.skillsync.sessionservice.feign.UserFeignClient;
import com.skillsync.sessionservice.repository.SessionRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final SessionRepository sessionRepository;
    private final RabbitTemplate rabbitTemplate;
    private final MentorFeignClient mentorFeignClient;
    private final UserFeignClient userFeignClient;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key.notification}")
    private String notificationRoutingKey;

    @CircuitBreaker(name = "mentorService", fallbackMethod = "bookSessionFallback")
    public Session bookSession(String learnerEmail, SessionDtos.SessionRequest request) {
        // Verify mentor exists via Feign + Circuit Breaker
        mentorFeignClient.getMentorById(request.getMentorId());

        // Verify learner exists via Feign
        try {
            userFeignClient.getUserByEmail(learnerEmail);
        } catch (Exception e) {
            log.warn("Could not verify learner, proceeding anyway: {}", e.getMessage());
        }

        Session session = new Session();
        session.setLearnerEmail(learnerEmail);
        session.setMentorId(request.getMentorId());
        session.setMentorEmail(request.getMentorEmail());
        session.setTopic(request.getTopic());
        session.setDescription(request.getDescription());
        session.setScheduledAt(request.getScheduledAt());
        session.setDurationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 60);
        session.setStatus(Session.SessionStatus.REQUESTED);
        Session saved = sessionRepository.save(session);

        sendNotification(request.getMentorEmail(), "New Session Request",
                "New session request from " + learnerEmail + " for: " + request.getTopic(), "SESSION_REQUESTED");
        sendNotification(learnerEmail, "Session Requested",
                "Your session request sent successfully. Topic: " + request.getTopic(), "SESSION_REQUESTED");
        return saved;
    }

    // Circuit Breaker fallback when mentor-service is down
    public Session bookSessionFallback(String learnerEmail, SessionDtos.SessionRequest request, Exception ex) {
        log.error("Circuit breaker triggered! Mentor service unavailable: {}", ex.getMessage());
        throw new RuntimeException("Mentor service is currently unavailable. Please try again later.");
    }

    @CircuitBreaker(name = "mentorService", fallbackMethod = "acceptSessionFallback")
    public Session acceptSession(Long id, String mentorEmail) {
        Session session = getById(id);
        if (!session.getMentorEmail().equals(mentorEmail)) {
            throw new RuntimeException("Only the session mentor can accept this session");
        }
        session.setStatus(Session.SessionStatus.ACCEPTED);
        Session saved = sessionRepository.save(session);
        sendNotification(session.getLearnerEmail(), "Session Accepted!",
                "Your session has been accepted. Scheduled at: " + session.getScheduledAt(), "SESSION_ACCEPTED");
        return saved;
    }

    public Session acceptSessionFallback(Long id, String mentorEmail, Exception ex) {
        log.error("Fallback for acceptSession: {}", ex.getMessage());
        throw new RuntimeException("Service unavailable. Please try again.");
    }

    public Session rejectSession(Long id, String mentorEmail, String reason) {
        Session session = getById(id);
        if (!session.getMentorEmail().equals(mentorEmail)) {
            throw new RuntimeException("Only the session mentor can reject this session");
        }
        session.setStatus(Session.SessionStatus.REJECTED);
        session.setRejectionReason(reason);
        Session saved = sessionRepository.save(session);
        sendNotification(session.getLearnerEmail(), "Session Rejected",
                "Your session was rejected. Reason: " + reason, "SESSION_REJECTED");
        return saved;
    }

    public Session completeSession(Long id) {
        Session session = getById(id);
        session.setStatus(Session.SessionStatus.COMPLETED);
        Session saved = sessionRepository.save(session);
        sendNotification(session.getLearnerEmail(), "Session Completed",
                "Session completed. Please submit a review!", "SESSION_COMPLETED");
        return saved;
    }

    public Session cancelSession(Long id) {
        Session session = getById(id);
        session.setStatus(Session.SessionStatus.CANCELLED);
        Session saved = sessionRepository.save(session);
        sendNotification(session.getMentorEmail(), "Session Cancelled",
                "Session scheduled at " + session.getScheduledAt() + " was cancelled.", "SESSION_CANCELLED");
        return saved;
    }

    public Session getById(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found: " + id));
    }

    public List<Session> getByLearner(String email) {
        return sessionRepository.findByLearnerEmail(email);
    }

    public List<Session> getByMentor(Long mentorId) {
        return sessionRepository.findByMentorId(mentorId);
    }

    public List<Session> getAll() {
        return sessionRepository.findAll();
    }

    private void sendNotification(String email, String subject, String message, String type) {
        try {
            rabbitTemplate.convertAndSend(exchange, notificationRoutingKey,
                    new SessionDtos.NotificationMessage(email, subject, message, type));
            log.info("Notification sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage());
        }
    }
}
