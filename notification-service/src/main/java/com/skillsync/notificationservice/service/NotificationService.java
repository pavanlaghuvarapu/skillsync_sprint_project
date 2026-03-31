package com.skillsync.notificationservice.service;

import com.skillsync.notificationservice.entity.Notification;
import com.skillsync.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository repository;

    public Notification save(String email, String subject, String message, String type) {
        Notification notification = new Notification();
        notification.setRecipientEmail(email);
        notification.setSubject(subject);
        notification.setMessage(message);
        notification.setNotificationType(type);
        notification.setRead(false);
        Notification saved = repository.save(notification);
        log.info("Notification saved for: {} type: {}", email, type);
        return saved;
    }

    public List<Notification> getAll(String email) {
        return repository.findByRecipientEmailOrderByCreatedAtDesc(email);
    }

    public List<Notification> getUnread(String email) {
        return repository.findByRecipientEmailAndIsReadFalse(email);
    }

    public Notification markAsRead(Long id) {
        Notification n = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        n.setRead(true);
        return repository.save(n);
    }

    public void markAllAsRead(String email) {
        List<Notification> unread = repository.findByRecipientEmailAndIsReadFalse(email);
        unread.forEach(n -> n.setRead(true));
        repository.saveAll(unread);
    }
}
