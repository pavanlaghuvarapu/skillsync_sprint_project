package com.skillsync.mentorservice.dto;

import lombok.Data;
import java.util.List;

public class MentorDtos {

    @Data
    public static class MentorRequest {
        private String bio;
        private Integer experienceYears;
        private Double pricePerHour;
        private String availability;
        private List<Long> skillIds;
    }

    @Data
    public static class SkillRequest {
        private String name;
        private String category;
        private String description;
    }
}
