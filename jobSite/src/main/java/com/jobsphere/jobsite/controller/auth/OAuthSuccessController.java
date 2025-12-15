package com.jobsphere.jobsite.controller.auth;

import com.jobsphere.jobsite.service.auth.GoogleAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class OAuthSuccessController {
    private final GoogleAuthService googleAuthService;

    @GetMapping("/oauth-success")
    public ResponseEntity<Map<String, Object>> handleOAuthSuccess(@AuthenticationPrincipal OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String googleId = oauth2User.getAttribute("sub");
        
        Map<String, Object> response = googleAuthService.handleGoogleLogin(email, name, googleId);
        return ResponseEntity.ok(response);
    }
}