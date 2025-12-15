package com.jobsphere.jobsite.config.security;

import com.jobsphere.jobsite.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final JwtUtils jwtUtils;

    public String createUserToken(String email, String userType) {
        return jwtUtils.generateToken(email, Map.of("userType", userType), null);
    }

    public String createAdminToken(String email) {
        return jwtUtils.generateToken(email, Map.of("userType", "ADMIN"), null);
    }

    public String createOtpToken(String email, String userType) {
        return jwtUtils.generateToken(email, Map.of(
            "userType", userType,
            "purpose", "OTP_VERIFICATION"
        ), 5 * 60 * 1000L);
    }

    public String createPasswordResetToken(String email) {
        return jwtUtils.generateToken(email, Map.of(
            "purpose", "PASSWORD_RESET"
        ), 15 * 60 * 1000L);
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