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
import java.util.UUID;

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
        if (userRepository.existsByEmail(email)) {
            throw new AuthException("Email already registered");
        }

        User user = User.builder()
            .email(email)
            .passwordHash(passwordEncoder.encode(password))
            .userType(userType)
            .emailVerified(false)
            .build();

        userRepository.save(user);
        
        otpService.sendOtp(email, OtpType.EMAIL_VERIFICATION);
        
        String otpToken = jwtTokenProvider.createOtpToken(email, userType.name());
        
        return Map.of(
            "message", "Check email for OTP",
            "otpToken", otpToken,
            "userId", user.getId()
        );
    }

    @Transactional
    public Map<String, Object> verifyOtp(String email, String otp, OtpType type) {
        boolean valid = otpService.validateOtp(email, otp, type);
        
        if (!valid) {
            throw new AuthException("Invalid OTP");
        }

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new AuthException("User not found"));

        if (type == OtpType.EMAIL_VERIFICATION) {
            user.setEmailVerified(true);
            userRepository.save(user);
        }

        otpRepository.markAllAsUsed(email, type);

        String token = jwtTokenProvider.createUserToken(email, user.getUserType().name());
        
        return Map.of(
            "token", token,
            "email", email,
            "userType", user.getUserType()
        );
    }

    public Map<String, Object> login(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new AuthException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new AuthException("Invalid credentials");
        }

        if (!user.isEmailVerified()) {
            throw new AuthException("Email not verified");
        }

        user.setLastLogin(Instant.now());
        userRepository.save(user);

        String token = jwtTokenProvider.createUserToken(email, user.getUserType().name());
        
        return Map.of(
            "token", token,
            "email", email,
            "userType", user.getUserType()
        );
    }

    public Map<String, Object> forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new AuthException("User not found"));
        
        otpService.sendOtp(email, OtpType.PASSWORD_RESET);
        
        return Map.of(
            "message", "Password reset OTP sent to email",
            "email", email
        );
    }

    public Map<String, Object> verifyResetOtp(String email, String otp) {
        boolean valid = otpService.validateOtp(email, otp, OtpType.PASSWORD_RESET);
        
        if (!valid) {
            throw new AuthException("Invalid OTP");
        }

        otpRepository.markAllAsUsed(email, OtpType.PASSWORD_RESET);

        String resetToken = jwtTokenProvider.createPasswordResetToken(email);
        
        return Map.of(
            "resetToken", resetToken,
            "email", email,
            "message", "OTP verified. You can now reset password."
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
            .orElseThrow(() -> new AuthException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return Map.of(
            "message", "Password reset successful",
            "email", email
        );
    }
}