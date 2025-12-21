
package com.jobsphere.jobsite.controller.admin;

import com.jobsphere.jobsite.config.security.JwtCookieService;
import com.jobsphere.jobsite.dto.admin.*;
import com.jobsphere.jobsite.service.admin.AdminAuthService;
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
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {
    private final AdminAuthService adminAuthService;
    private final JwtCookieService jwtCookieService;
    private final LogoutService logoutService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody AdminLoginRequest request) {
        return ResponseEntity.ok(adminAuthService.login(request.getEmail(), request.getPassword()));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@Valid @RequestBody AdminOtpRequest request,
            HttpServletResponse response) {
        Map<String, Object> result = adminAuthService.verifyOtp(request.getEmail(), request.getOtp());
        String accessToken = (String) result.get("token");
        String refreshToken = (String) result.get("refreshToken");
        jwtCookieService.setAdminCookies(response, accessToken, refreshToken);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestParam String email) {
        return ResponseEntity.ok(adminAuthService.forgotPassword(email));
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<Map<String, Object>> verifyResetOtp(@Valid @RequestBody AdminVerifyResetOtpRequest request) {
        return ResponseEntity.ok(adminAuthService.verifyResetOtp(request.getEmail(), request.getOtp()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody AdminResetPasswordRequest request) {
        return ResponseEntity.ok(adminAuthService.resetPasswordWithToken(
                request.getResetToken(), request.getNewPassword(), request.getConfirmPassword()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getCookieValue(request, "admin_refresh_token");
        logoutService.logoutAdmin(response, refreshToken);
        return ResponseEntity.ok(Map.of("message", "Admin logged out", "timestamp", Instant.now()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getCookieValue(request, "admin_refresh_token");
        if (refreshToken == null) {
            return ResponseEntity.status(401).body(Map.of("error", "No refresh token"));
        }
        Map<String, Object> result = adminAuthService.refreshAccessToken(refreshToken);
        String newAccessToken = (String) result.get("accessToken");
        String newRefreshToken = (String) result.get("refreshToken");
        jwtCookieService.setAdminCookies(response, newAccessToken, newRefreshToken);
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
