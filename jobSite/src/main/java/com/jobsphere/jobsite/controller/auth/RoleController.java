package com.jobsphere.jobsite.controller.auth;

import com.jobsphere.jobsite.constant.UserType;
import com.jobsphere.jobsite.dto.auth.SetRoleRequest;
import jakarta.validation.Valid;
import com.jobsphere.jobsite.service.auth.GoogleAuthService;
import com.jobsphere.jobsite.service.auth.AuthService;
import com.jobsphere.jobsite.config.security.JwtCookieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class RoleController {
    private final GoogleAuthService googleAuthService;
    private final AuthService authService;
    private final JwtCookieService jwtCookieService;

    @PostMapping("/select-role")
    @Transactional
    public ResponseEntity<Map<String, Object>> selectRole(
            @Valid @RequestBody SelectRoleRequest request,
            HttpServletResponse response) {

        if (request.getTempToken() == null) {
            throw new AuthException("Temp token required");
        }

        String email = jwtTokenProvider.getSubject(request.getTempToken());
        Map<String, Object> claims = jwtTokenProvider.getClaims(request.getTempToken());

        if (!"ROLE_SELECTION".equals(claims.get("purpose"))) {
            throw new AuthException("Invalid token for role selection");
        }

        Map<String, Object> result;

        if (request.getGoogleId() != null) {
            result = googleAuthService.createUserFromGoogle(
                email, request.getName(), request.getGoogleId(), request.getUserType());
        } else {
            result = updateEmailUserRoleManaged(email, request.getUserType());
        }

        String accessToken = (String) result.get("token");
        String resultEmail = (String) result.get("email");

        // Create Refresh Token
        String refreshToken = authService.createRefreshToken(resultEmail);

        // Set Cookies
        jwtCookieService.setUserCookies(response, accessToken, refreshToken);


        return ResponseEntity.ok(result);
    }

    private Map<String, Object> updateEmailUserRoleManaged(String email, UserType userType) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new AuthException("User not found"));

        if (user.getUserType() != null) {
            throw new AuthException("User already has a role assigned");
        }

        user.setUserType(userType);
        user.setLastLogin(Instant.now());
        // No need to call save(user); the user is managed in the same @Transactional call

        String accessToken = jwtTokenProvider.createUserToken(email, userType.name());
        String refreshToken = authService.createRefreshToken(email);

        return Map.of(
            "token", accessToken,
            "refreshToken", refreshToken,
            "email", email,
            "userType", userType,
            "needsRoleSelection", false,
            "message", "Role selected successfully"
        );
    }
}