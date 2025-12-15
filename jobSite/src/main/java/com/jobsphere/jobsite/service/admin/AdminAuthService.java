package com.jobsphere.jobsite.service.admin;

import com.jobsphere.jobsite.config.security.JwtTokenProvider;
import com.jobsphere.jobsite.constant.OtpType;
import com.jobsphere.jobsite.exception.AuthException;
import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.model.admin.Admin;
import com.jobsphere.jobsite.repository.UserRepository;
import com.jobsphere.jobsite.repository.admin.AdminRepository;
import com.jobsphere.jobsite.service.auth.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminAuthService {
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final OtpService otpService;

    public Map<String, Object> login(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new AuthException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new AuthException("Invalid credentials");
        }

        otpService.sendOtp(email, OtpType.ADMIN_LOGIN);
        
        String otpToken = jwtTokenProvider.createOtpToken(email, "ADMIN");
        
        return Map.of(
            "message", "Admin OTP sent to email",
            "otpToken", otpToken
        );
    }

    @Transactional
    public Map<String, Object> verifyOtp(String email, String otp) {
        boolean valid = otpService.validateOtp(email, otp, OtpType.ADMIN_LOGIN);
        
        if (!valid) {
            throw new AuthException("Invalid OTP");
        }

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new AuthException("User not found"));

        user.setLastLogin(Instant.now());
        userRepository.save(user);

        String token = jwtTokenProvider.createAdminToken(email);
        
        return Map.of(
            "token", token,
            "email", email,
            "role", "ADMIN"
        );
    }

    public Map<String, Object> forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new AuthException("Admin not found"));
        
        otpService.sendOtp(email, OtpType.ADMIN_LOGIN);
        
        return Map.of(
            "message", "Admin password reset OTP sent to email",
            "email", email
        );
    }

    public Map<String, Object> verifyResetOtp(String email, String otp) {
        boolean valid = otpService.validateOtp(email, otp, OtpType.ADMIN_LOGIN);
        
        if (!valid) {
            throw new AuthException("Invalid OTP");
        }

        String resetToken = jwtTokenProvider.createPasswordResetToken(email);
        
        return Map.of(
            "resetToken", resetToken,
            "email", email,
            "message", "OTP verified. You can now reset admin password."
        );
    }

    @Transactional
    public Map<String, Object> resetPasswordWithToken(String resetToken, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new AuthException("Passwords do not match");
        }
        
        if (!jwtTokenProvider.validate(resetToken)) {
            throw new AuthException("Invalid or expired reset token");
        }
        
        Map<String, Object> claims = jwtTokenProvider.getClaims(resetToken);
        String purpose = (String) claims.get("purpose");
        
        if (!"PASSWORD_RESET".equals(purpose)) {
            throw new AuthException("Invalid reset token");
        }
        
        String email = jwtTokenProvider.getSubject(resetToken);
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new AuthException("Admin not found"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return Map.of(
            "message", "Admin password reset successful",
            "email", email
        );
    }
}