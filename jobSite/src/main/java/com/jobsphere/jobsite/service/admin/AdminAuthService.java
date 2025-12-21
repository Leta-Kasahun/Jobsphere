package com.jobsphere.jobsite.service.admin;

import com.jobsphere.jobsite.config.security.JwtTokenProvider;
import com.jobsphere.jobsite.constant.OtpType;
import com.jobsphere.jobsite.exception.AuthException;
import com.jobsphere.jobsite.model.admin.Admin;
import com.jobsphere.jobsite.model.auth.RefreshToken;
import com.jobsphere.jobsite.repository.admin.AdminRepository;
import com.jobsphere.jobsite.repository.auth.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminAuthService {
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AdminOtpService adminOtpService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh.expiration:604800}")
    private long refreshExpirationSeconds;

    public Map<String, Object> login(String email, String password) {
        Admin admin = adminRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AuthException("Invalid credentials"));

        if (!passwordEncoder.matches(password, admin.getPasswordHash())) {
            throw new AuthException("Invalid credentials");
        }

        adminOtpService.sendAdminLoginOtp(admin);

        String otpToken = jwtTokenProvider.createToken(email,
                Map.of("purpose", "ADMIN_OTP_VERIFICATION", "userType", "ADMIN"),
                10 * 60 * 1000L); // 10 minutes

        return Map.of(
                "message", "Admin OTP sent to email",
                "otpToken", otpToken,
                "email", email);
    }

    @Transactional
    public Map<String, Object> verifyOtp(String email, String otp) {
        Admin admin = adminRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AuthException("Admin not found"));

        boolean valid = adminOtpService.validateAdminOtp(admin, otp, OtpType.ADMIN_LOGIN);

        if (!valid) {
            throw new AuthException("Invalid OTP");
        }

        admin.setLastLoginAt(Instant.now());
        adminRepository.save(admin);

        String accessToken = jwtTokenProvider.createAdminToken(email);
        String refreshToken = createAdminRefreshToken(email);

        return Map.of(
                "token", accessToken,
                "refreshToken", refreshToken,
                "email", email,
                "role", "ADMIN");
    }

    public Map<String, Object> forgotPassword(String email) {
        Admin admin = adminRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AuthException("Admin not found"));

        adminOtpService.sendAdminPasswordResetOtp(admin);

        return Map.of(
                "message", "Admin password reset OTP sent to email",
                "email", email);
    }

    public Map<String, Object> verifyResetOtp(String email, String otp) {
        Admin admin = adminRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AuthException("Admin not found"));

        boolean valid = adminOtpService.validateAdminOtp(admin, otp, OtpType.PASSWORD_RESET);

        if (!valid) {
            throw new AuthException("Invalid OTP");
        }

        String resetToken = jwtTokenProvider.createPasswordResetToken(email);

        return Map.of(
                "resetToken", resetToken,
                "email", email,
                "message", "OTP verified. You can now reset admin password.");
    }

    @Transactional
    public Map<String, Object> resetPasswordWithToken(String resetToken, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new AuthException("Passwords do not match");
        }

        if (!jwtTokenProvider.validate(resetToken)) {
            throw new AuthException("Invalid or expired reset token");
        }

        var claims = jwtTokenProvider.getClaims(resetToken);
        String purpose = (String) claims.get("purpose");
        if (!"PASSWORD_RESET".equals(purpose)) {
            throw new AuthException("Invalid reset token");
        }

        String email = jwtTokenProvider.getSubject(resetToken);

        Admin admin = adminRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AuthException("Admin not found"));

        admin.setPasswordHash(passwordEncoder.encode(newPassword));
        adminRepository.save(admin);

        return Map.of(
                "message", "Admin password reset successful",
                "email", email);
    }

    public Map<String, Object> refreshAccessToken(String refreshToken) {
        if (!jwtTokenProvider.validate(refreshToken)) {
            throw new AuthException("Invalid refresh token");
        }

        Map<String, Object> claims = jwtTokenProvider.getClaims(refreshToken);
        if (!"REFRESH".equals(claims.get("type"))) {
            throw new AuthException("Invalid token type");
        }

        String email = jwtTokenProvider.getSubject(refreshToken);
        Admin admin = adminRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AuthException("Admin not found"));

        return Map.of(
                "accessToken", jwtTokenProvider.createAdminToken(email),
                "refreshToken", createAdminRefreshToken(email),
                "email", email,
                "role", "ADMIN");
    }

    private String createAdminRefreshToken(String email) {
        return jwtTokenProvider.createToken(email,
                Map.of("type", "REFRESH", "userType", "ADMIN"),
                refreshExpirationSeconds * 1000L);
    }
}