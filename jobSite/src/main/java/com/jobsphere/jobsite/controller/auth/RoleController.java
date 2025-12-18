package com.jobsphere.jobsite.controller.auth;
import com.jobsphere.jobsite.constant.UserType;
import com.jobsphere.jobsite.config.security.JwtCookieService;
import com.jobsphere.jobsite.config.security.JwtTokenProvider;
import com.jobsphere.jobsite.dto.auth.SelectRoleRequest;
import com.jobsphere.jobsite.exception.AuthException;
import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.repository.UserRepository;
import com.jobsphere.jobsite.service.auth.AuthService;
import com.jobsphere.jobsite.service.auth.GoogleAuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

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
            result = updateEmailUserRole(email, request.getUserType());
        }
        
        String accessToken = (String) result.get("token");
        String refreshToken = authService.createRefreshToken(email);
        jwtCookieService.setUserCookies(response, accessToken, refreshToken);
        
        return ResponseEntity.ok(result);
    }
    
    private Map<String, Object> updateEmailUserRole(String email, com.jobsphere.jobsite.constant.UserType userType) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new AuthException("User not found"));
        
        if (user.getUserType() != null) {
            throw new AuthException("User already has a role assigned");
        }
        
        user.setUserType(userType);
        user.setLastLogin(Instant.now());
        userRepository.save(user);
        
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