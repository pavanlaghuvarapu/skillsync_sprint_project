package com.skillsync.notificationservice.consumer;

import com.skillsync.notificationservice.config.NotificationMessage;
import com.skillsync.notificationservice.feign.UserFeignClient;
import com.skillsync.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;
    private final UserFeignClient userFeignClient;

    @RabbitListener(queues = "${rabbitmq.queue.notification}")
    public void consume(NotificationMessage message) {
        log.info("Received from RabbitMQ: type={} recipient={}",
                message.getNotificationType(), message.getRecipientEmail());
        try {
            // Feign - verify user exists before saving
            try {
                userFeignClient.getUserByEmail(message.getRecipientEmail());
            } catch (Exception e) {
                log.warn("Could not verify user, saving notification anyway");
            }
            notificationService.save(
                    message.getRecipientEmail(),
                    message.getSubject(),
                    message.getMessage(),
                    message.getNotificationType()
            );
        } catch (Exception e) {
            log.error("Error processing notification: {}", e.getMessage());
        }
    }
}
