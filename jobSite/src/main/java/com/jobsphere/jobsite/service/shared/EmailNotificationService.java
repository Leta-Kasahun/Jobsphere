package com.jobsphere.jobsite.service.shared;

import com.jobsphere.jobsite.utils.EmailTemplateBuilder;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {
    private final JavaMailSender mailSender;
    private final EmailTemplateBuilder emailTemplateBuilder;

    @Value("${jobsphere.mail.from.email}")
    private String fromEmail;

    @Value("${jobsphere.mail.from.name}")
    private String fromName;

    public void sendVerificationCode(String toEmail, String companyName, String verificationCode) {
        try {
            String subject = "Your Company Verification Code - JobSphere";
            String htmlContent = emailTemplateBuilder.buildVerificationCodeEmail(companyName, verificationCode);

            sendEmail(toEmail, subject, htmlContent);
            log.info("Verification code sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification code to {}", toEmail, e);
            throw new RuntimeException("Failed to send verification code email");
        }
    }

    public void sendVerificationRejection(String toEmail, String companyName, String rejectionReason) {
        try {
            String subject = "Company Verification Update - JobSphere";
            String htmlContent = emailTemplateBuilder.buildRejectionEmail(companyName, rejectionReason);

            sendEmail(toEmail, subject, htmlContent);
            log.info("Verification rejection notification sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send rejection notification to {}", toEmail, e);
            throw new RuntimeException("Failed to send rejection notification email");
        }
    }

    public void sendApprovalNotification(String toEmail, String companyName) {
        try {
            String subject = "Company Verification Approved - JobSphere";
            String htmlContent = emailTemplateBuilder.buildApprovalEmail(companyName);

            sendEmail(toEmail, subject, htmlContent);
            log.info("Approval notification sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send approval notification to {}", toEmail, e);
            throw new RuntimeException("Failed to send approval notification email");
        }
    }

    public void sendCustomEmail(String toEmail, String subject, String htmlContent) {
        try {
            sendEmail(toEmail, subject, htmlContent);
            log.info("Custom notification sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send custom notification to {}", toEmail, e);
            throw new RuntimeException("Failed to send custom notification email");
        }
    }

    private void sendEmail(String toEmail, String subject, String htmlContent) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail, fromName);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}



