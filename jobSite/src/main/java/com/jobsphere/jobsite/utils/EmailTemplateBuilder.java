package com.jobsphere.jobsite.utils;

import com.jobsphere.jobsite.constant.OtpType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class EmailTemplateBuilder {

    public String buildOtpEmail(String otpCode, OtpType otpType) {
        String templateName = getTemplateName(otpType);

        try {
            String template = loadTemplate("email/" + templateName + "-email.html");
            return template.replace("{{otpCode}}", otpCode);
        } catch (Exception e) {
            log.warn("Template {} not found, using fallback", templateName);
            return buildFallbackEmail(otpCode, otpType);
        }
    }

    public String buildVerificationCodeEmail(String companyName, String verificationCode) {
        try {
            String template = loadTemplate("email/company-verification-code-email.html");
            return template.replace("{{companyName}}", companyName)
                    .replace("{{verificationCode}}", verificationCode);
        } catch (Exception e) {
            log.warn("Company verification code template not found, using fallback");
            return """
                    <div style="font-family:system-ui;max-width:600px;margin:auto">
                        <h2 style="color:#1d4ed8">Verification for %s</h2>
                        <p>Use the following code to verify your company on JobSphere:</p>
                        <div style="font-size:36px;font-weight:bold;color:#1d4ed8;margin:20px 0;letter-spacing:5px">
                            %s
                        </div>
                    </div>
                    """.formatted(companyName, verificationCode);
        }
    }

    public String buildApprovalEmail(String companyName) {
        try {
            String template = loadTemplate("email/company-approval-email.html");
            return template.replace("{{companyName}}", companyName);
        } catch (Exception e) {
            log.warn("Company approval email template not found, using fallback");
            return """
                    <div style="font-family:system-ui;max-width:600px;margin:auto">
                        <h2 style="color:#10b981">Verification Approved!</h2>
                        <p>Congratulations, <strong>%s</strong> has been successfully verified on JobSphere.</p>
                        <p>You now have full access to employer features.</p>
                    </div>
                    """.formatted(companyName);
        }
    }

    public String buildRejectionEmail(String companyName, String rejectionReason) {
        try {
            String template = loadTemplate("email/company-rejection-email.html");
            return template.replace("{{companyName}}", companyName)
                    .replace("{{rejectionReason}}", rejectionReason);
        } catch (Exception e) {
            log.warn("Company rejection email template not found, using fallback");
            return """
                    <div style="font-family:system-ui;max-width:600px;margin:auto">
                        <h2 style="color:#ef4444">Verification Update</h2>
                        <p>We were unable to verify <strong>%s</strong>.</p>
                        <p><strong>Reason:</strong> %s</p>
                        <p>You can submit a new request with corrected information.</p>
                    </div>
                    """.formatted(companyName, rejectionReason);
        }
    }

    private String loadTemplate(String path) throws Exception {
        ClassPathResource resource = new ClassPathResource("templates/" + path);
        byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public String getEmailSubject(OtpType otpType) {
        return switch (otpType) {
            case EMAIL_VERIFICATION -> "Verify Your JobSphere Account";
            case PASSWORD_RESET -> "Reset Your JobSphere Password";
            case ADMIN_LOGIN -> "Your Admin Login Code - JobSphere";
        };
    }

    private String getTemplateName(OtpType otpType) {
        return switch (otpType) {
            case PASSWORD_RESET -> "reset-password";
            case ADMIN_LOGIN -> "otp";
            default -> "verification";
        };
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