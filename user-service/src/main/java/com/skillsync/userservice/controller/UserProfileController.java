package com.skillsync.userservice.controller;

import com.skillsync.userservice.dto.UserProfileDto;
import com.skillsync.userservice.entity.UserProfile;
import com.skillsync.userservice.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile management and image upload")
public class UserProfileController {

    private final UserProfileService service;

    @Operation(summary = "Create or update own profile", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfile> createOrUpdate(Authentication auth, @RequestBody UserProfileDto dto) {
        return ResponseEntity.ok(service.createOrUpdateProfile(auth.getName(), dto));
    }

    @Operation(summary = "Get my profile", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/profile/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfile> getMyProfile(Authentication auth) {
        return ResponseEntity.ok(service.getByEmail(auth.getName()));
    }

    @Operation(summary = "Get profile by email", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/profile/email/{email}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfile> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(service.getByEmail(email));
    }

    @Operation(summary = "Get profile by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/profile/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfile> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(summary = "Get all users - ADMIN only", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserProfile>> getAllUsers() {
        return ResponseEntity.ok(service.getAllUsers());
    }

    @Operation(summary = "Upload profile image", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/profile/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> uploadImage(Authentication auth,
                                               @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(service.uploadImage(auth.getName(), file));
    }

    @Operation(summary = "View/download profile image (public)")
    @GetMapping("/images/{filename}")
    public ResponseEntity<byte[]> downloadImage(@PathVariable String filename) throws IOException {
        byte[] image = service.downloadImage(filename);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image);
    }
}
