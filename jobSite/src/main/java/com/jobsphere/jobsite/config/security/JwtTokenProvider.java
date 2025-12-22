package com.jobsphere.jobsite.config.security;

import com.jobsphere.jobsite.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final JwtUtils jwtUtils;

    public String createUserToken(String email, String userType, java.util.UUID userId) {
        return jwtUtils.generateToken(email, Map.of(
                "userType", userType,
                "userId", userId.toString()), null);
    }

    public String createAdminToken(String email, java.util.UUID userId) {
        return jwtUtils.generateToken(email, Map.of(
                "userType", "ADMIN",
                "userId", userId.toString()), null);
    }

    public String createOtpToken(String email, String userType) {
        Map<String, Object> claims = userType != null ? Map.of(
                "userType", userType,
                "purpose", "OTP_VERIFICATION") : Map.of("purpose", "OTP_VERIFICATION");
        return jwtUtils.generateToken(email, claims, 5 * 60 * 1000L);
    }

    public String createPasswordResetToken(String email) {
        return jwtUtils.generateToken(email, Map.of(
                "purpose", "PASSWORD_RESET"), 15 * 60 * 1000L);
    }

    // Generic token creator
    public String createToken(String subject, Map<String, Object> claims, Long customExpirationMs) {
        return jwtUtils.generateToken(subject, claims, customExpirationMs);
    }

    public boolean validate(String token) {
        return jwtUtils.validate(token);
    }

    public String getSubject(String token) {
        return jwtUtils.getSubject(token);
    }

    public String getUserType(String token) {
        try {
            return jwtUtils.parseClaims(token).get("userType", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public Map<String, Object> getClaims(String token) {
        return jwtUtils.parseClaims(token);
    }
}
