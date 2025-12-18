package com.jobsphere.jobsite.controller.auth;

import com.jobsphere.jobsite.config.security.JwtCookieService;
import com.jobsphere.jobsite.constant.OtpType;
import com.jobsphere.jobsite.dto.auth.*;
import com.jobsphere.jobsite.service.auth.AuthService;
import com.jobsphere.jobsite.service.auth.LogoutService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtCookieService jwtCookieService;
    private final LogoutService logoutService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(
                request.getEmail(), request.getPassword(), request.getUserType()));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@Valid @RequestBody OtpRequest request, HttpServletResponse response) {
        OtpType type = request.getType() == null ? OtpType.EMAIL_VERIFICATION : request.getType();
        Map<String, Object> result = authService.verifyOtp(request.getEmail(), request.getOtp(), type);
        if (type == OtpType.EMAIL_VERIFICATION && result.get("token") != null) {
            String accessToken = (String) result.get("token");
            String refreshToken = (String) result.get("refreshToken");
            jwtCookieService.setUserCookies(response, accessToken, refreshToken);
        }
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        Map<String, Object> result = authService.login(request.getEmail(), request.getPassword());
        String accessToken = (String) result.get("token");
        String refreshToken = authService.createRefreshToken(request.getEmail());
        jwtCookieService.setUserCookies(response, accessToken, refreshToken);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request.getEmail()));
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<Map<String, Object>> verifyResetOtp(@Valid @RequestBody VerifyResetOtpRequest request) {
        return ResponseEntity.ok(authService.verifyResetOtp(request.getEmail(), request.getOtp()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @Valid @RequestBody ResetPasswordWithTokenRequest request) {
        return ResponseEntity.ok(authService.resetPasswordWithToken(
                request.getResetToken(), request.getNewPassword(), request.getConfirmPassword()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getCookieValue(request, "refresh_token");
        if (refreshToken != null) {
            logoutService.logoutUser(response, refreshToken);
        }
        return ResponseEntity.ok(Map.of("message", "Logged out", "timestamp", Instant.now()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getCookieValue(request, "refresh_token");
        if (refreshToken == null) {
            return ResponseEntity.status(401).body(Map.of("error", "No refresh token"));
        }
        Map<String, Object> result = authService.refreshAccessToken(refreshToken);
        String newAccessToken = (String) result.get("accessToken");
        String newRefreshToken = (String) result.get("refreshToken");
        jwtCookieService.setUserCookies(response, newAccessToken, newRefreshToken);
        return ResponseEntity.ok(result);
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null)
            return null;
        return Arrays.stream(request.getCookies())
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}