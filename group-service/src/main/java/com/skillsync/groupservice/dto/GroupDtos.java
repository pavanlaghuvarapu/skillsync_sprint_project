package com.skillsync.groupservice.dto;

import lombok.Data;

public class GroupDtos {

    @Data
    public static class GroupRequest {
        private String name;
        private String description;
        private String topic;
        private Integer maxMembers;
    }

    @Data
    public static class MessageRequest {
        private String message;
    }
}
