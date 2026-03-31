package com.skillsync.authservice.controller;

import com.skillsync.authservice.dto.AuthDtos;
import com.skillsync.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Register, Login, Logout, Validate Token")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register new user", description = "Register as ROLE_LEARNER, ROLE_MENTOR or ROLE_ADMIN")
    @PostMapping("/register")
    public ResponseEntity<AuthDtos.AuthResponse> register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Login", description = "Returns JWT token. Copy token and use in Authorize button above.")
    @PostMapping("/login")
    public ResponseEntity<AuthDtos.AuthResponse> login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Validate JWT token")
    @GetMapping("/validate")
    public ResponseEntity<Boolean> validate(@RequestParam String token) {
        return ResponseEntity.ok(authService.validateToken(token));
    }

    @Operation(summary = "Logout - blacklists token in Redis")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String token) {
        authService.logout(token);
        return ResponseEntity.ok("Logged out successfully");
    }
}
