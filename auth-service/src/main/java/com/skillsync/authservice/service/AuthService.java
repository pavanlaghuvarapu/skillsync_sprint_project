package com.skillsync.authservice.service;

import com.skillsync.authservice.dto.AuthDtos;
import com.skillsync.authservice.entity.User;
import com.skillsync.authservice.repository.UserRepository;
import com.skillsync.authservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.valueOf(request.getRole()));
        User saved = userRepository.save(user);

        String token = jwtUtil.generateToken(saved.getEmail(), saved.getRole().name(), saved.getId());
        redisTemplate.opsForValue().set("token:" + saved.getEmail(), token, 24, TimeUnit.HOURS);
        return new AuthDtos.AuthResponse(token, saved.getEmail(), saved.getRole().name(), saved.getFullName(), saved.getId());
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
        redisTemplate.opsForValue().set("token:" + user.getEmail(), token, 24, TimeUnit.HOURS);
        return new AuthDtos.AuthResponse(token, user.getEmail(), user.getRole().name(), user.getFullName(), user.getId());
    }

    public boolean validateToken(String token) {
        if (!jwtUtil.isTokenValid(token)) return false;
        String blacklisted = redisTemplate.opsForValue().get("blacklist:" + token);
        return blacklisted == null;
    }

    public void logout(String token) {
        redisTemplate.opsForValue().set("blacklist:" + token, "true", 24, TimeUnit.HOURS);
        String email = jwtUtil.extractEmail(token);
        redisTemplate.delete("token:" + email);
    }
}
