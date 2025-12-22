package com.jobsphere.jobsite.dto.cvtemplate;

import java.util.Map;

public record CVTemplateUpdateRequest(
    String name,
    String category,
    String status,
    String description,
    Map<String, Object> sections
) {}
