
package com.jobsphere.jobsite.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/verify-otp",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/api/v1/admin/auth/login",
            "/api/v1/admin/auth/verify-otp",
            "/api/v1/auth/refresh",
            "/api/v1/admin/auth/refresh",
            "/api/v1/auth/complete-registration",
            "/api/v1/auth/oauth-success",
            "/oauth2/authorization/**",
            "/login/oauth2/code/**",
            "/api/v1/public/**",
            "/actuator/health",
            "/actuator/info");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return PUBLIC_ENDPOINTS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        logger.debug(String.format("JWT_FILTER: Processing request to %s", path));

        String token = getTokenFromRequest(request);

        // If there is no token, continue (security will handle protected endpoints)
        if (token == null) {
            chain.doFilter(request, response);
            return;
        }
        // immediately.
        String authHeader = request.getHeader("Authorization");
        boolean hasAuthHeader = authHeader != null && authHeader.startsWith("Bearer ");

        if (!jwtTokenProvider.validate(token)) {
            if (hasAuthHeader) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            } else {
                // If token from cookie and invalid, let the request continue as anonymous
                chain.doFilter(request, response);
                return;
            }
        }

        String email = jwtTokenProvider.getSubject(token);
        if (email == null) {
            chain.doFilter(request, response);
            return;
        }

        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);

            if (path.startsWith("/api/v1/admin/")) {
                logger.info(String.format("AUTH_SUCCESS: Admin %s accessing %s with authorities %s",
                        email, path, userDetails.getAuthorities()));
            }
        } catch (Exception e) {
            logger.error(String.format("AUTH_ERROR: Failed to load user %s - %s", email, e.getMessage()));
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        chain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        // Check Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // Check cookies
        Cookie[] cookies = request.getCookies();
        if (cookies == null)
            return null;

        String path = request.getRequestURI();
        String cookieName = path.startsWith("/api/v1/admin/") ? "admin_access_token" : "access_token";

        return Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
