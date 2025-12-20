package com.jobsphere.jobsite.dto.auth;

import com.jobsphere.jobsite.constant.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SetRoleRequest {
    @NotBlank
    private String token;

    @NotNull
    private UserType userType;
}
