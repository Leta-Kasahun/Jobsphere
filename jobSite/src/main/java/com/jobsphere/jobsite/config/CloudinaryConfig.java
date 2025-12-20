package com.jobsphere.jobsite.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {
    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @Value("${cloudinary.url:}")
    private String cloudinaryUrl;

    @PostConstruct
    public void validateConfig() {
        if ((cloudinaryUrl == null || cloudinaryUrl.isBlank()) &&
                (cloudName == null || cloudName.isBlank() || apiKey == null || apiKey.isBlank() || apiSecret == null
                        || apiSecret.isBlank())) {
            System.err.println("CRITICAL ERROR: Cloudinary credentials are not configured in application.properties.");
        } else {
            System.out.println("Cloudinary configuration loaded.");
        }
    }

    @Bean
    public Cloudinary cloudinary() {
        if (cloudinaryUrl != null && !cloudinaryUrl.isBlank()) {
            return new Cloudinary(cloudinaryUrl.trim());
        }
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName.trim(),
                "api_key", apiKey.trim(),
                "api_secret", apiSecret.trim(),
                "secure", true));
    }
}