package com.jobsphere.jobsite.service.admin;

import com.jobsphere.jobsite.config.security.JwtTokenProvider;
import com.jobsphere.jobsite.constant.OtpType;
import com.jobsphere.jobsite.exception.AuthException;
import com.jobsphere.jobsite.model.admin.Admin;
import com.jobsphere.jobsite.model.auth.RefreshToken;
import com.jobsphere.jobsite.repository.admin.AdminRepository;
import com.jobsphere.jobsite.repository.auth.RefreshTokenRepository;
import com.jobsphere.jobsite.service.auth.OtpService;
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
    private final OtpService otpService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh.expiration:604800}")
    private long refreshExpirationSeconds;

    public Map<String, Object> login(String email, String password) {
        Admin admin = adminRepository.findByEmail(email)
            .orElseThrow(() -> new AuthException("Invalid credentials"));

        if (!passwordEncoder.matches(password, admin.getPasswordHash())) {
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

        Admin admin = adminRepository.findByEmail(email)
            .orElseThrow(() -> new AuthException("Admin not found"));

        admin.setLastLoginAt(Instant.now());
        adminRepository.save(admin);

        String accessToken = jwtTokenProvider.createAdminToken(email);

        // Create refresh token (raw) and persist a hash of it (BCrypt for now; deterministic hashing can be added later)
        String refreshToken = jwtTokenProvider.createToken(email, Map.of("type", "REFRESH"), refreshExpirationSeconds * 1000L);
        String refreshHash = passwordEncoder.encode(refreshToken);

        RefreshToken rt = RefreshToken.builder()
            .user(null) // storing admin refresh token without user association (nullable)
            .tokenHash(refreshHash)
            .expiresAt(Instant.now().plusSeconds(refreshExpirationSeconds))
            .revoked(false)
            .build();

        refreshTokenRepository.save(rt);

        return Map.of(
            "token", accessToken,
            "refreshToken", refreshToken,
            "email", email,
            "role", "ADMIN"
        );
    }

    public Map<String, Object> forgotPassword(String email) {
        adminRepository.findByEmail(email).orElseThrow(() -> new AuthException("Admin not found"));

        otpService.sendOtp(email, OtpType.PASSWORD_RESET);

        return Map.of(
            "message", "Admin password reset OTP sent to email",
            "email", email
        );
    }

    public Map<String, Object> verifyResetOtp(String email, String otp) {
        boolean valid = otpService.validateOtp(email, otp, OtpType.PASSWORD_RESET);

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

        var claims = jwtTokenProvider.getClaims(resetToken);
        String purpose = (String) claims.get("purpose");

        if (!"PASSWORD_RESET".equals(purpose)) {
            throw new AuthException("Invalid reset token");
        }

        String email = jwtTokenProvider.getSubject(resetToken);

        Admin admin = adminRepository.findByEmail(email)
            .orElseThrow(() -> new AuthException("Admin not found"));

        admin.setPasswordHash(passwordEncoder.encode(newPassword));
        adminRepository.save(admin);

        return Map.of(
            "message", "Admin password reset successful",
            "email", email
        );
    }
}
