package com.jobsphere.jobsite.service.auth;

import com.jobsphere.jobsite.constant.OtpType;
import com.jobsphere.jobsite.exception.AuthException;
import com.jobsphere.jobsite.model.auth.Otp;
import com.jobsphere.jobsite.repository.auth.OtpRepository;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class OtpService {
    private final OtpRepository otpRepository;
    private final OtpGenerator otpGenerator;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final EmailTemplateBuilder emailTemplateBuilder;
    
    @Value("${jobsphere.mail.from.email}")
    private String fromEmail;
    
    @Value("${jobsphere.mail.from.name}")
    private String fromName;

    @Transactional
    public void sendOtp(String email, OtpType type) {
        String otpCode = otpGenerator.generateSixDigitOtp();
        String hashedOtp = passwordEncoder.encode(otpCode);

        Otp otp = Otp.builder()
            .email(email)
            .codeHash(hashedOtp)
            .type(type)
            .expiresAt(Instant.now().plusSeconds(600))
            .used(false)
            .build();

        otpRepository.save(otp);
        sendEmail(email, otpCode, type);
    }

    @Transactional
    public boolean validateOtp(String email, String otpCode, OtpType type) {
        var otpOpt = otpRepository.findByEmailAndTypeAndUsedFalseAndExpiresAtAfter(
            email, type, Instant.now());

        if (otpOpt.isEmpty()) {
            return false;
        }

        Otp otp = otpOpt.get();
        
        if (passwordEncoder.matches(otpCode, otp.getCodeHash())) {
            otp.setUsed(true);
            otpRepository.save(otp);
            return true;
        }

        otp.setAttempts(otp.getAttempts() + 1);
        otpRepository.save(otp);
        return false;
    }

    private void sendEmail(String toEmail, String otpCode, OtpType type) {
        try {
            String subject = emailTemplateBuilder.getEmailSubject(type);
            String htmlContent = emailTemplateBuilder.buildOtpEmail(otpCode, type);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("{} OTP sent to {}", type, toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP to {}", toEmail, e);
            throw new AuthException("Failed to send OTP");
        }
    }
}