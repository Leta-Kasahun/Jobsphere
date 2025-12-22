package com.jobsphere.jobsite.dto.cvtemplate;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record CVTemplateResponse(
    UUID id,
    String name,
    String category,
    String status,
    String description,
    Map<String, Object> sections,
    Instant createdAt,
    Instant updatedAt
) {}
