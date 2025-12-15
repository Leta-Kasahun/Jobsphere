package com.jobsphere.jobsite.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyResetOtpRequest {
    @NotBlank @Email
    private String email;
    
    @NotBlank
    private String otp;
}