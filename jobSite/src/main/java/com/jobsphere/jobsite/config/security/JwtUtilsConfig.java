package com.jobsphere.jobsite.config.security;

import com.jobsphere.jobsite.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class JwtUtilsConfig {
    private final JwtProperties jwtProperties;

    @Bean
    public JwtUtils jwtUtils() {
        return new JwtUtils(jwtProperties.getSecret(), jwtProperties.getExpirationMs());
    }
}