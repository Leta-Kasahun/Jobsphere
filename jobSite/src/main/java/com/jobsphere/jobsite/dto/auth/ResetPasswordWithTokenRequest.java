package com.jobsphere.jobsite.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordWithTokenRequest {
    @NotBlank
    private String resetToken;
    
    @NotBlank @Size(min = 6)
    private String newPassword;
    
    @NotBlank @Size(min = 6)
    private String confirmPassword;
}