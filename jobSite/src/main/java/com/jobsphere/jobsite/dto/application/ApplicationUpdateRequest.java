package com.jobsphere.jobsite.dto.application;

public record ApplicationUpdateRequest(
    String status,
    Boolean hiredFlag,
    String notes
) {}
