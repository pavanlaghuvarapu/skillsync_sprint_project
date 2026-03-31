package com.skillsync.notificationservice.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    private String recipientEmail;
    private String subject;
    private String message;
    private String notificationType;
}
