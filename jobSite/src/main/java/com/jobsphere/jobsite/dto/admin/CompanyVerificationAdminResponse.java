package com.jobsphere.jobsite.dto.admin;

import java.time.Instant;
import java.util.UUID;

public record CompanyVerificationAdminResponse(
    UUID id,
    UUID userId,
    String userEmail,
    String companyName,
    String tradeLicenseUrl,
    String tinNumber,
    String website,
    String status,
    Instant submittedAt,
    Instant reviewedAt
) {}
