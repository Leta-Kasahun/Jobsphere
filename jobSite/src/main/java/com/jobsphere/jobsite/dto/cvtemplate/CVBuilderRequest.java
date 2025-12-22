package com.jobsphere.jobsite.dto.cvtemplate;

import java.util.Map;
import java.util.UUID;

public record CVBuilderRequest(
    UUID templateId,
    Map<String, Object> filledData
) {}
