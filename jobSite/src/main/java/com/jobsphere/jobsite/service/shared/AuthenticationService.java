package com.jobsphere.jobsite.service.shared;

import com.jobsphere.jobsite.repository.UserRepository;
import com.jobsphere.jobsite.repository.admin.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    public UUID getCurrentUserId() {
        String email = getCurrentUserEmail();

        // 1. Try regular user
        var user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            return user.get().getId();
        }

        // 2. Try admin
        var admin = adminRepository.findByEmail(email);
        if (admin.isPresent()) {
            return admin.get().getId();
        }

        throw new IllegalStateException("User or Admin not found: " + email);
    }

    public String getCurrentUserEmail() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            throw new IllegalStateException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.User user) {
            return user.getUsername();
        }
        return principal.toString();
    }
}



