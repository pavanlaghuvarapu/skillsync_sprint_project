package com.skillsync.sessionservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

public class SessionDtos {

    @Data
    public static class SessionRequest {
        private Long mentorId;
        private String mentorEmail;
        private String topic;
        private String description;
        private LocalDateTime scheduledAt;
        private Integer durationMinutes;
    }

    @Data
    public static class NotificationMessage {
        private String recipientEmail;
        private String subject;
        private String message;
        private String notificationType;

        public NotificationMessage(String recipientEmail, String subject,
                                    String message, String notificationType) {
            this.recipientEmail = recipientEmail;
            this.subject = subject;
            this.message = message;
            this.notificationType = notificationType;
        }
    }
}
