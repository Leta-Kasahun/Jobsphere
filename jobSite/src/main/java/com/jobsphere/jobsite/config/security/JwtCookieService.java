package com.jobsphere.jobsite.config.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtCookieService {
    
    @Value("${jwt.access.expiration:900}")
    private int accessExpiration;
    
    @Value("${jwt.refresh.expiration:604800}")
    private int refreshExpiration;
    
    public void setUserCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        setCookie(response, "access_token", accessToken, "/", accessExpiration);
        setCookie(response, "refresh_token", refreshToken, "/api/v1/auth/refresh", refreshExpiration);
    }
    
    public void setAdminCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        setCookie(response, "admin_access_token", accessToken, "/api/v1/admin", accessExpiration);
        setCookie(response, "admin_refresh_token", refreshToken, "/api/v1/admin/auth/refresh", refreshExpiration);
    }
    
    public void clearUserCookies(HttpServletResponse response) {
        clearCookie(response, "access_token", "/");
        clearCookie(response, "refresh_token", "/api/v1/auth/refresh");
    }
    
    public void clearAdminCookies(HttpServletResponse response) {
        clearCookie(response, "admin_access_token", "/api/v1/admin");
        clearCookie(response, "admin_refresh_token", "/api/v1/admin/auth/refresh");
    }
    
    private void setCookie(HttpServletResponse response, String name, String value, String path, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
    
    private void clearCookie(HttpServletResponse response, String name, String path) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath(path);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}