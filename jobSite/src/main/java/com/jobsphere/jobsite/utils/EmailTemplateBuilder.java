package com.jobsphere.jobsite.utils;

import com.jobsphere.jobsite.constant.OtpType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@Slf4j
public class EmailTemplateBuilder {
    
    public String buildOtpEmail(String otpCode, OtpType otpType) {
        String templateName = getTemplateName(otpType);
        
        try {
            ClassPathResource resource = new ClassPathResource(
                "templates/email/" + templateName + "-email.html");
            byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
            String template = new String(bytes, StandardCharsets.UTF_8);
            return template.replace("{{otpCode}}", otpCode);
        } catch (Exception e) {
            log.warn("Template {} not found, using fallback", templateName);
            return buildFallbackEmail(otpCode, otpType);
        }
    }
    
    public String getEmailSubject(OtpType otpType) {
        return switch (otpType) {
            case EMAIL_VERIFICATION -> "Verify Your JobSphere Account";
            case PASSWORD_RESET -> "Reset Your JobSphere Password";
            case ADMIN_LOGIN -> "Your Admin Login Code - JobSphere";
        };
    }
    
    private String getTemplateName(OtpType otpType) {
        return otpType == OtpType.PASSWORD_RESET ? "reset-password" : "verification";
    }
    
    public String buildVerificationCodeEmail(String verificationCode) {
        return """
            <div style="font-family:system-ui;max-width:600px;margin:auto">
                <h2 style="color:#1d4ed8">Your Company Verification Code</h2>
                <div style="font-size:36px;font-weight:bold;color:#1d4ed8;margin:20px 0">
                    %s
                </div>
                <p>Use this code to complete your company verification</p>
                <p>This code can only be used once.</p>
            </div>
            """.formatted(verificationCode);
    }

    public String buildRejectionEmail(String companyName, String rejectionReason) {
        return """
            <div style="font-family:system-ui;max-width:600px;margin:auto">
                <h2 style="color:#dc2626">Company Verification Update</h2>
                <p>Dear Employer,</p>
                <p>We regret to inform you that your company verification request for <strong>%s</strong> has been rejected.</p>
                <p><strong>Reason:</strong> %s</p>
                <p>You can submit a new verification request with corrected information.</p>
                <p>Best regards,<br>JobSphere Team</p>
            </div>
            """.formatted(companyName, rejectionReason);
    }

    public String buildApprovalEmail(String companyName) {
        return """
            <div style="font-family:system-ui;max-width:600px;margin:auto">
                <h2 style="color:#16a34a">Company Verification Approved!</h2>
                <p>Congratulations! Your company <strong>%s</strong> has been verified on JobSphere.</p>
                <p>You now have access to post jobs and manage your company profile.</p>
                <p>Best regards,<br>JobSphere Team</p>
            </div>
            """.formatted(companyName);
    }

    private String buildFallbackEmail(String otpCode, OtpType otpType) {
        String purpose = switch (otpType) {
            case EMAIL_VERIFICATION -> "email verification";
            case PASSWORD_RESET -> "password reset";
            case ADMIN_LOGIN -> "admin login";
        };

        return """
            <div style="font-family:system-ui;max-width:600px;margin:auto">
                <h2 style="color:#1d4ed8">Your %s Code</h2>
                <div style="font-size:36px;font-weight:bold;color:#1d4ed8;margin:20px 0">
                    %s
                </div>
                <p>Valid for 10 minutes</p>
            </div>
            """.formatted(purpose, otpCode);
    }
}