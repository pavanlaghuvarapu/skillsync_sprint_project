package com.skillsync.userservice.dto;

import lombok.Data;

@Data
public class UserProfileDto {
    private String email;
    private String fullName;
    private String bio;
    private String profileImageUrl;
    private String phoneNumber;
    private String location;
    private String linkedinUrl;
    private String githubUrl;
}
