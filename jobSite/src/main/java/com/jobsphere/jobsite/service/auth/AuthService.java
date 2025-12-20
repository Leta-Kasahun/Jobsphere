package com.jobsphere.jobsite.service.auth;

import com.jobsphere.jobsite.config.security.JwtTokenProvider;
import com.jobsphere.jobsite.constant.OtpType;
import com.jobsphere.jobsite.constant.UserType;
import com.jobsphere.jobsite.exception.AuthException;
import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.repository.UserRepository;
import com.jobsphere.jobsite.repository.auth.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final OtpService otpService;
    private final OtpRepository otpRepository;

    @Transactional
    public Map<String, Object> register(String email, String password, UserType userType) {
        if (userRepository.existsByEmail(email))
            throw new AuthException("Email already registered");

        User user = User.builder()
                .email(email).passwordHash(passwordEncoder.encode(password))
                .userType(userType).emailVerified(false).build();

        userRepository.save(user);
        otpService.sendOtp(email, OtpType.EMAIL_VERIFICATION);

        return Map.of("message", "Check email for OTP",
                "otpToken", jwtTokenProvider.createOtpToken(email, userType != null ? userType.name() : null),
                "userId", user.getId());
    }

    @Transactional
    public Map<String, Object> verifyOtp(String email, String otp, OtpType type) {
        if (!otpService.validateOtp(email, otp, type))
            throw new AuthException("Invalid OTP");

        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new AuthException("User not found"));

        if (type == OtpType.EMAIL_VERIFICATION) {
            user.setEmailVerified(true);
            userRepository.save(user);
        }

        otpRepository.markAllAsUsed(email, type);

        if (type == OtpType.EMAIL_VERIFICATION && user.getUserType() == null) {
            String tempToken = jwtTokenProvider.createToken(email,
                    Map.of("purpose", "ROLE_SELECTION"),
                    10 * 60 * 1000L); // 10 minutes
            return Map.of(
                    "needsRoleSelection", true,
                    "email", email,
                    "tempToken", tempToken);
        }

        String accessToken = jwtTokenProvider.createUserToken(email, user.getUserType().name());
        String refreshToken = createRefreshToken(email);

        return Map.of("token", accessToken, "refreshToken", refreshToken, "email", email, "userType",
                user.getUserType());
    }

    public Map<String, Object> login(String email, String password) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AuthException("Invalid credentials"));

        // If the user signed up with OAuth (no password) inform client to use Google
        // login
        if (user.getPasswordHash() == null)
            throw new AuthException("Please use Google login");

        if (!passwordEncoder.matches(password, user.getPasswordHash()))
            throw new AuthException("Invalid credentials");
        if (!user.isEmailVerified())
            throw new AuthException("Email not verified");

        user.setLastLogin(Instant.now());
        userRepository.save(user);

        // Safely handle null userType
        String userTypeName = user.getUserType() != null ? user.getUserType().name() : "UNKNOWN";
        return Map.of("token", jwtTokenProvider.createUserToken(email, userTypeName),
                "email", email, "userType", userTypeName);
    }

    public Map<String, Object> forgotPassword(String email) {
        userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new AuthException("User not found"));
        otpService.sendOtp(email, OtpType.PASSWORD_RESET);
        return Map.of("message", "Password reset OTP sent to email", "email", email);
    }

    @Transactional
    public Map<String, Object> verifyResetOtp(String email, String otp) {
        if (!otpService.validateOtp(email, otp, OtpType.PASSWORD_RESET))
            throw new AuthException("Invalid OTP");

        otpRepository.markAllAsUsed(email, OtpType.PASSWORD_RESET);

        return Map.of("resetToken", jwtTokenProvider.createPasswordResetToken(email),
                "email", email, "message", "OTP verified");
    }

    @Transactional
    public Map<String, Object> resetPasswordWithToken(String resetToken, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword))
            throw new AuthException("Passwords do not match");
        if (!jwtTokenProvider.validate(resetToken))
            throw new AuthException("Invalid reset token");

        Map<String, Object> claims = jwtTokenProvider.getClaims(resetToken);
        if (!"PASSWORD_RESET".equals(claims.get("purpose")))
            throw new AuthException("Invalid reset token");

        String email = jwtTokenProvider.getSubject(resetToken);
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new AuthException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return Map.of("message", "Password reset successful", "email", email);
    }

    public String createRefreshToken(String email) {
        return jwtTokenProvider.createToken(email, Map.of("type", "REFRESH"), 604800000L);
    }

    public Map<String, Object> refreshAccessToken(String refreshToken) {
        if (!jwtTokenProvider.validate(refreshToken))
            throw new AuthException("Invalid refresh token");

        Map<String, Object> claims = jwtTokenProvider.getClaims(refreshToken);
        if (!"REFRESH".equals(claims.get("type")))
            throw new AuthException("Invalid token type");

        String email = jwtTokenProvider.getSubject(refreshToken);
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new AuthException("User not found"));

        String userTypeName = user.getUserType() != null ? user.getUserType().name() : "UNKNOWN";
        return Map.of("accessToken", jwtTokenProvider.createUserToken(email, userTypeName),
                "refreshToken", createRefreshToken(email), "email", email, "userType", userTypeName);
    }

    @Transactional
    public Map<String, Object> finalizeRole(String tempToken, UserType userType) {
        if (!jwtTokenProvider.validate(tempToken))
            throw new AuthException("Invalid or expired token");

        Map<String, Object> claims = jwtTokenProvider.getClaims(tempToken);
        if (!"ROLE_SELECTION".equals(claims.get("purpose")))
            throw new AuthException("Invalid token purpose");

        String email = jwtTokenProvider.getSubject(tempToken);
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new AuthException("User not found"));

        user.setUserType(userType);
        userRepository.save(user);

        String accessToken = jwtTokenProvider.createUserToken(email, userType.name());
        String refreshToken = createRefreshToken(email);

        return Map.of("token", accessToken, "refreshToken", refreshToken, "email", email, "userType", userType);
    }
}