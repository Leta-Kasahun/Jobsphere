
package com.jobsphere.jobsite.controller.auth;

import com.jobsphere.jobsite.constant.UserType;
import com.jobsphere.jobsite.service.auth.GoogleAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class RoleController {
    private final GoogleAuthService googleAuthService;

    @PostMapping("/select-role")
    public ResponseEntity<Map<String, Object>> selectRole(
            @RequestParam String email,
            @RequestParam String googleId,
            @RequestParam String name,
            @RequestParam UserType userType) {
        
        Map<String, Object> response = googleAuthService.createUserFromGoogle(
            email, name, googleId, userType);
        
        return ResponseEntity.ok(response);
    }
}
