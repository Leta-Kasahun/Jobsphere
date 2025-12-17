package com.jobsphere.jobsite.controller.auth;

import com.jobsphere.jobsite.service.auth.GoogleAuthService;
import com.jobsphere.jobsite.service.auth.AuthService;
import com.jobsphere.jobsite.config.security.JwtCookieService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class OAuthSuccessController {
    private final GoogleAuthService googleAuthService;
    private final AuthService authService;
    private final JwtCookieService jwtCookieService;

    @GetMapping("/oauth-success")
    public void handleOAuthSuccess(@AuthenticationPrincipal OAuth2User oauth2User, HttpServletResponse response)
            throws java.io.IOException {
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String googleId = oauth2User.getAttribute("sub");

        Map<String, Object> result = googleAuthService.handleGoogleLogin(email, name, googleId);

        String frontendUrl = "http://localhost:5173"; // Should be in properties but hardcoded for now as per env

        if ((Boolean) result.get("needsRoleSelection")) {
            String redirectUrl = String.format("%s/auth/select-role?email=%s&name=%s&googleId=%s",
                    frontendUrl, email, name, googleId);
            response.sendRedirect(redirectUrl);
        } else {
            String token = (String) result.get("token");
            String resultEmail = (String) result.get("email");
            String userType = result.get("userType").toString();

            // Create Refresh Token
            String refreshToken = authService.createRefreshToken(resultEmail);

            // Set Cookies (HttpOnly)
            jwtCookieService.setUserCookies(response, token, refreshToken);

            String redirectUrl = String.format("%s/auth/google-callback?token=%s&email=%s&userType=%s",
                    frontendUrl, token, resultEmail, userType);
            response.sendRedirect(redirectUrl);
        }
    }
}
