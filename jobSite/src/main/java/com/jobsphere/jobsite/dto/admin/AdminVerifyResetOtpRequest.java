package com.jobsphere.jobsite.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminVerifyResetOtpRequest {
    @NotBlank @Email
    private String email;
    
    @NotBlank
    private String otp;
}