package com.jobsphere.jobsite.service.notification;

import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.model.notification.Notification;
import com.jobsphere.jobsite.repository.UserRepository;
import com.jobsphere.jobsite.repository.notification.NotificationRepository;
import com.jobsphere.jobsite.service.shared.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailNotificationService emailNotificationService;

    public Page<Notification> getUserNotifications(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        notificationRepository.markAsRead(notificationId, userId);
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsRead(userId);
    }

    @Transactional
    @Async
    public void createNotification(UUID userId, String title, String message) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null)
            return;

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .build();

        notificationRepository.save(notification);
    }

    @Transactional
    @Async
    public void createNotificationWithEmail(UUID userId, String title, String message, String emailSubject,
            String emailContent) {
        log.info("Processing notification with email for userId: {}", userId);
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("User with id {} not found for notification", userId);
            return;
        }

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .build();

        notification = notificationRepository.save(notification);
        log.info("Notification {} saved for user {}", notification.getId(), userId);

        try {
            log.info("Sending email to {}", user.getEmail());
            emailNotificationService.sendCustomEmail(user.getEmail(), emailSubject, emailContent);
            log.info("Email sent successfully to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email notification to user {} at {}", userId, user.getEmail(), e);
        }
    }

    @Async
    public void notifyNewApplication(UUID employerUserId, String jobTitle) {
        log.info("Starting notifyNewApplication for employer: {} and job: {}", employerUserId, jobTitle);
        String title = "New Job Application";
        String message = "You have a new application for: " + jobTitle;
        String emailSubject = "New Application - " + jobTitle;
        String emailContent = "<p>You have received a new application for your job posting: <strong>" + jobTitle
                + "</strong></p>" +
                "<p>Please log in to your dashboard to review the application.</p>";

        createNotificationWithEmail(employerUserId, title, message, emailSubject, emailContent);
    }

    @Async
    public void notifyApplicationStatusUpdate(UUID seekerUserId, String jobTitle, String status) {
        log.info("Starting notifyApplicationStatusUpdate for seeker: {} and job: {}", seekerUserId, jobTitle);
        String title = "Application Status Update";
        String message = "Your application for " + jobTitle + " is now " + status.toLowerCase();
        String emailSubject = "Application Update - " + jobTitle;
        String emailContent = "<p>Your application status for <strong>" + jobTitle
                + "</strong> has been updated to: <strong>" + status + "</strong></p>" +
                "<p>Please log in to your dashboard for more details.</p>";

        createNotificationWithEmail(seekerUserId, title, message, emailSubject, emailContent);
    }

    @Async
    public void notifyJobLike(UUID employerUserId, String jobTitle, String seekerName) {
        log.info("Starting notifyJobLike for employer: {} and job: {}", employerUserId, jobTitle);
        String title = "New Job Interest";
        String message = seekerName + " liked your job posting: " + jobTitle;
        String emailSubject = "Interest in " + jobTitle;
        String emailContent = "<p>A seeker (<strong>" + seekerName
                + "</strong>) has expressed interest in your job posting: <strong>" + jobTitle + "</strong></p>" +
                "<p>Check your dashboard for more details.</p>";

        createNotificationWithEmail(employerUserId, title, message, emailSubject, emailContent);
    }
}
