package com.jobsphere.jobsite.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;

public final class SecurityUtils {
    private SecurityUtils() {}
    
    public static Optional<String> currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            return Optional.empty();
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.User user) {
            return Optional.of(user.getUsername());
        }
        return Optional.of(principal.toString());
    }
    
    public static boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(role::equals);
    }
}