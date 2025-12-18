package com.jobsphere.jobsite.dto.auth;

import com.jobsphere.jobsite.constant.UserType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
public class SelectRoleRequest {
    @NotNull
    private UserType userType;
    
    private String tempToken;
    private String googleId;
    private String name;
}