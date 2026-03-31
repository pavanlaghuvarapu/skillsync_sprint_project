package com.skillsync.sessionservice.controller;

import com.skillsync.sessionservice.dto.SessionDtos;
import com.skillsync.sessionservice.entity.Session;
import com.skillsync.sessionservice.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Tag(name = "Sessions", description = "Session booking lifecycle: REQUESTED -> ACCEPTED -> COMPLETED")
@SecurityRequirement(name = "bearerAuth")
public class SessionController {

    private final SessionService sessionService;

    @Operation(summary = "Book a session - LEARNER only")
    @PostMapping("/book")
    @PreAuthorize("hasRole('ROLE_LEARNER')")
    public ResponseEntity<Session> book(Authentication auth, @RequestBody SessionDtos.SessionRequest request) {
        return ResponseEntity.ok(sessionService.bookSession(auth.getName(), request));
    }

    @Operation(summary = "Accept a session - MENTOR only")
    @PutMapping("/{id}/accept")
    @PreAuthorize("hasRole('ROLE_MENTOR')")
    public ResponseEntity<Session> accept(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(sessionService.acceptSession(id, auth.getName()));
    }

    @Operation(summary = "Reject a session - MENTOR only")
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ROLE_MENTOR')")
    public ResponseEntity<Session> reject(@PathVariable Long id, Authentication auth, @RequestParam String reason) {
        return ResponseEntity.ok(sessionService.rejectSession(id, auth.getName(), reason));
    }

    @Operation(summary = "Complete a session - MENTOR or ADMIN")
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('ROLE_MENTOR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<Session> complete(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.completeSession(id));
    }

    @Operation(summary = "Cancel a session - any authenticated user")
    @PutMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Session> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.cancelSession(id));
    }

    @Operation(summary = "Get session by ID")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Session> getById(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getById(id));
    }

    @Operation(summary = "Get my sessions (learner)")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Session>> getMySessions(Authentication auth) {
        return ResponseEntity.ok(sessionService.getByLearner(auth.getName()));
    }

    @Operation(summary = "Get sessions by learner email")
    @GetMapping("/learner/{email}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Session>> getByLearner(@PathVariable String email) {
        return ResponseEntity.ok(sessionService.getByLearner(email));
    }

    @Operation(summary = "Get sessions by mentor ID")
    @GetMapping("/mentor/{mentorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Session>> getByMentor(@PathVariable Long mentorId) {
        return ResponseEntity.ok(sessionService.getByMentor(mentorId));
    }

    @Operation(summary = "Get all sessions - ADMIN only")
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<Session>> getAll() {
        return ResponseEntity.ok(sessionService.getAll());
    }
}
