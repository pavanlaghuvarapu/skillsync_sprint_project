package com.skillsync.authservice;

import com.skillsync.authservice.dto.AuthDtos;
import com.skillsync.authservice.entity.User;
import com.skillsync.authservice.repository.UserRepository;
import com.skillsync.authservice.security.JwtUtil;
import com.skillsync.authservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
    	lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);        }

    @Test
    void register_ShouldSucceed_WhenEmailNotExists() {
        // Arrange
        AuthDtos.RegisterRequest request = new AuthDtos.RegisterRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("password123");
        request.setFullName("Test User");
        request.setRole("ROLE_LEARNER");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("test@gmail.com");
        savedUser.setFullName("Test User");
        savedUser.setRole(User.Role.ROLE_LEARNER);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken(anyString(), anyString(), any())).thenReturn("mock.jwt.token");

        // Act
        AuthDtos.AuthResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals("test@gmail.com", response.getEmail());
        assertEquals("mock.jwt.token", response.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ShouldThrow_WhenEmailAlreadyExists() {
        // Arrange
        AuthDtos.RegisterRequest request = new AuthDtos.RegisterRequest();
        request.setEmail("existing@gmail.com");
        request.setPassword("password123");
        request.setFullName("Existing User");
        request.setRole("ROLE_LEARNER");

        when(userRepository.existsByEmail("existing@gmail.com")).thenReturn(true);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(request));
        assertEquals("Email already registered", ex.getMessage());
    }

    @Test
    void login_ShouldSucceed_WithValidCredentials() {
        // Arrange
        AuthDtos.LoginRequest request = new AuthDtos.LoginRequest();
        request.setEmail("pavan@gmail.com");
        request.setPassword("pavan123");

        User user = new User();
        user.setId(1L);
        user.setEmail("pavan@gmail.com");
        user.setPassword("encodedPassword");
        user.setFullName("Pavan Kumar");
        user.setRole(User.Role.ROLE_LEARNER);

        when(userRepository.findByEmail("pavan@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pavan123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString(), any())).thenReturn("mock.jwt.token");

        // Act
        AuthDtos.AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("pavan@gmail.com", response.getEmail());
        assertEquals("ROLE_LEARNER", response.getRole());
        assertEquals("mock.jwt.token", response.getToken());
    }

    @Test
    void login_ShouldThrow_WithInvalidPassword() {
        // Arrange
        AuthDtos.LoginRequest request = new AuthDtos.LoginRequest();
        request.setEmail("pavan@gmail.com");
        request.setPassword("wrongpassword");

        User user = new User();
        user.setEmail("pavan@gmail.com");
        user.setPassword("encodedPassword");
        user.setRole(User.Role.ROLE_LEARNER);

        when(userRepository.findByEmail("pavan@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(request));
        assertEquals("Invalid email or password", ex.getMessage());
    }

    @Test
    void login_ShouldThrow_WhenUserNotFound() {
        // Arrange
        AuthDtos.LoginRequest request = new AuthDtos.LoginRequest();
        request.setEmail("notfound@gmail.com");
        request.setPassword("password");

        when(userRepository.findByEmail("notfound@gmail.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authService.login(request));
    }

    @Test
    void validateToken_ShouldReturnTrue_ForValidToken() {
        when(jwtUtil.isTokenValid("valid.token")).thenReturn(true);
        when(valueOperations.get("blacklist:valid.token")).thenReturn(null);

        assertTrue(authService.validateToken("valid.token"));
    }

    @Test
    void validateToken_ShouldReturnFalse_ForBlacklistedToken() {
        when(jwtUtil.isTokenValid("blacklisted.token")).thenReturn(true);
        when(valueOperations.get("blacklist:blacklisted.token")).thenReturn("true");

        assertFalse(authService.validateToken("blacklisted.token"));
    }
}
