package com.jobsphere.jobsite.service.auth;

import com.jobsphere.jobsite.config.security.JwtCookieService;
import com.jobsphere.jobsite.repository.auth.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutService {
    private final JwtCookieService jwtCookieService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void logoutUser(HttpServletResponse response, String refreshTokenHash) {
        if (refreshTokenHash != null) {
            refreshTokenRepository.findByTokenHash(refreshTokenHash)
                    .ifPresent(token -> token.setRevoked(true));
        }
        jwtCookieService.clearUserCookies(response);
    }

    @Transactional
    public void logoutAdmin(HttpServletResponse response, String refreshTokenHash) {
        if (refreshTokenHash != null) {
            refreshTokenRepository.findByTokenHash(refreshTokenHash)
                    .ifPresent(token -> token.setRevoked(true));
        }
        jwtCookieService.clearAdminCookies(response);
    }
}
