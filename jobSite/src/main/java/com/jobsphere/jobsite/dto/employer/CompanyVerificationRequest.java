package com.jobsphere.jobsite.dto.employer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record CompanyVerificationRequest(
    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name must be less than 255 characters")
    String companyName,

    MultipartFile tradeLicense,

    @Size(max = 50, message = "TIN number must be less than 50 characters")
    String tinNumber,

    @Pattern(regexp = "^https?://.*", message = "Website must be a valid URL")
    @Size(max = 255, message = "Website must be less than 255 characters")
    String website
) {}
