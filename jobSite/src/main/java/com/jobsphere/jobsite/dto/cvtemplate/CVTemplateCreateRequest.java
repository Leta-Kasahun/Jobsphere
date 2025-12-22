package com.jobsphere.jobsite.dto.cvtemplate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record CVTemplateCreateRequest(
    @NotBlank(message = "Name is required")
    String name,

    @NotBlank(message = "Category is required")
    String category,

    String description,

    @NotNull(message = "Sections are required")
    Map<String, Object> sections
) {}
