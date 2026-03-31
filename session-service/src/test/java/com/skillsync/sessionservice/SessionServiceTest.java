package com.skillsync.sessionservice;

import com.skillsync.sessionservice.dto.SessionDtos;
import com.skillsync.sessionservice.entity.Session;
import com.skillsync.sessionservice.feign.MentorFeignClient;
import com.skillsync.sessionservice.feign.UserFeignClient;
import com.skillsync.sessionservice.repository.SessionRepository;
import com.skillsync.sessionservice.service.SessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private MentorFeignClient mentorFeignClient;

    @Mock
    private UserFeignClient userFeignClient;

    @InjectMocks
    private SessionService sessionService;

    @Test
    void bookSession_ShouldSaveSession_WhenMentorExists() {
        // Arrange
        ReflectionTestUtils.setField(sessionService, "exchange", "skillsync.exchange");
        ReflectionTestUtils.setField(sessionService, "notificationRoutingKey", "notification.routing.key");

        SessionDtos.SessionRequest request = new SessionDtos.SessionRequest();
        request.setMentorId(1L);
        request.setMentorEmail("mentor@gmail.com");
        request.setTopic("Spring Boot");
        request.setScheduledAt(LocalDateTime.now().plusDays(1));
        request.setDurationMinutes(60);

        Session savedSession = new Session();
        savedSession.setId(1L);
        savedSession.setStatus(Session.SessionStatus.REQUESTED);
        savedSession.setLearnerEmail("learner@gmail.com");
        savedSession.setMentorEmail("mentor@gmail.com");

        when(mentorFeignClient.getMentorById(1L)).thenReturn(ResponseEntity.ok(new Object()));
        when(userFeignClient.getUserByEmail(anyString())).thenReturn(ResponseEntity.ok(new Object()));
        when(sessionRepository.save(any(Session.class))).thenReturn(savedSession);

        // Act
        Session result = sessionService.bookSession("learner@gmail.com", request);

        // Assert
        assertNotNull(result);
        assertEquals(Session.SessionStatus.REQUESTED, result.getStatus());
        verify(sessionRepository).save(any(Session.class));
        verify(rabbitTemplate, atLeastOnce())
        .convertAndSend(anyString(), anyString(), any(Object.class));        }

    @Test
    void acceptSession_ShouldChangeStatusToAccepted() {
        // Arrange
        ReflectionTestUtils.setField(sessionService, "exchange", "skillsync.exchange");
        ReflectionTestUtils.setField(sessionService, "notificationRoutingKey", "notification.routing.key");

        Session session = new Session();
        session.setId(1L);
        session.setMentorEmail("mentor@gmail.com");
        session.setLearnerEmail("learner@gmail.com");
        session.setScheduledAt(LocalDateTime.now().plusDays(1));
        session.setStatus(Session.SessionStatus.REQUESTED);

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(Session.class))).thenReturn(session);

        // Act
        Session result = sessionService.acceptSession(1L, "mentor@gmail.com");

        // Assert
        assertEquals(Session.SessionStatus.ACCEPTED, result.getStatus());
    }

    @Test
    void acceptSession_ShouldThrow_WhenWrongMentor() {
        Session session = new Session();
        session.setId(1L);
        session.setMentorEmail("mentor@gmail.com");
        session.setStatus(Session.SessionStatus.REQUESTED);

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        assertThrows(RuntimeException.class,
                () -> sessionService.acceptSession(1L, "wrong@gmail.com"));
    }

    @Test
    void getById_ShouldThrow_WhenSessionNotFound() {
        when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> sessionService.getById(99L));
    }

    @Test
    void completeSession_ShouldChangeStatusToCompleted() {
        ReflectionTestUtils.setField(sessionService, "exchange", "skillsync.exchange");
        ReflectionTestUtils.setField(sessionService, "notificationRoutingKey", "notification.routing.key");

        Session session = new Session();
        session.setId(1L);
        session.setLearnerEmail("learner@gmail.com");
        session.setMentorEmail("mentor@gmail.com");
        session.setStatus(Session.SessionStatus.ACCEPTED);

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any())).thenReturn(session);

        Session result = sessionService.completeSession(1L);

        assertEquals(Session.SessionStatus.COMPLETED, result.getStatus());
    }
}
