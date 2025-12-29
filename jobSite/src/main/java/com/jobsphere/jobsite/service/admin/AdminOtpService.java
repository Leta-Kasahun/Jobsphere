package com.jobsphere.jobsite.service.admin;

import com.jobsphere.jobsite.constant.OtpType;
import com.jobsphere.jobsite.exception.AuthException;
import com.jobsphere.jobsite.model.admin.Admin;
import com.jobsphere.jobsite.model.admin.AdminOtp;
import com.jobsphere.jobsite.repository.admin.AdminOtpRepository;
import com.jobsphere.jobsite.utils.EmailTemplateBuilder;
import com.jobsphere.jobsite.utils.OtpGenerator;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminOtpService {
    private final AdminOtpRepository adminOtpRepository;
    private final OtpGenerator otpGenerator;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final EmailTemplateBuilder emailTemplateBuilder;

    @Value("${jobsphere.mail.from.email}")
    private String fromEmail;

    @Value("${jobsphere.mail.from.name}")
    private String fromName;

    @Transactional
    public void sendAdminLoginOtp(Admin admin) {
        sendOtp(admin, OtpType.ADMIN_LOGIN);
    }

    @Transactional
    public void sendAdminPasswordResetOtp(Admin admin) {
        sendOtp(admin, OtpType.PASSWORD_RESET);
    }

    private void sendOtp(Admin admin, OtpType type) {
        String otpCode = otpGenerator.generateSixDigitOtp();
        String hashedOtp = passwordEncoder.encode(otpCode);

        AdminOtp adminOtp = AdminOtp.builder()
                .admin(admin)
                .codeHash(hashedOtp)
                .type(type)
                .expiresAt(Instant.now().plusSeconds(600)) // 10 minutes
                .used(false)
                .build();

        adminOtpRepository.save(adminOtp);
        sendEmail(admin.getEmail(), otpCode, type);
    }

    @Transactional
    public boolean validateAdminOtp(Admin admin, String otpCode, OtpType type) {
        List<AdminOtp> otps = adminOtpRepository.findByAdminAndTypeAndUsedFalseAndExpiresAtAfter(
                admin, type, Instant.now());

        if (otps.isEmpty()) {
            return false;
        }

        for (AdminOtp otp : otps) {
            if (passwordEncoder.matches(otpCode, otp.getCodeHash())) {
                otp.setUsed(true);
                adminOtpRepository.save(otp);
                return true;
            }
        }

        return false;
    }

    private void sendEmail(String toEmail, String otpCode, OtpType type) {
        try {
            // REUSE YOUR EXISTING TEMPLATE BUILDER
            String subject = emailTemplateBuilder.getEmailSubject(type);
            String htmlContent = emailTemplateBuilder.buildOtpEmail(otpCode, type);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Use same sender settings
            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Admin {} OTP sent to {}", type, toEmail);
        } catch (Exception e) {
            log.error("Failed to send admin OTP to {}", toEmail, e);
            throw new AuthException("Failed to send admin OTP");
        }
    }
}