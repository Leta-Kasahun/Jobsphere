
package com.jobsphere.jobsite.controller.auth;

import com.jobsphere.jobsite.constant.UserType;
import com.jobsphere.jobsite.service.auth.GoogleAuthService;
import com.jobsphere.jobsite.service.auth.AuthService;
import com.jobsphere.jobsite.config.security.JwtCookieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class RoleController {
    private final GoogleAuthService googleAuthService;
    private final AuthService authService;
    private final JwtCookieService jwtCookieService;

    @PostMapping("/select-role")
    public ResponseEntity<Map<String, Object>> selectRole(
            @RequestParam String email,
            @RequestParam String googleId,
            @RequestParam String name,
            @RequestParam UserType userType,
            jakarta.servlet.http.HttpServletResponse response) {

        Map<String, Object> result = googleAuthService.createUserFromGoogle(
                email, name, googleId, userType);

        String accessToken = (String) result.get("token");
        String resultEmail = (String) result.get("email");

        // Create Refresh Token
        String refreshToken = authService.createRefreshToken(resultEmail);

        // Set Cookies
        jwtCookieService.setUserCookies(response, accessToken, refreshToken);

        return ResponseEntity.ok(result);
    }
}
