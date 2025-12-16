package com.jobsphere.jobsite.dto.auth;

import com.jobsphere.jobsite.constant.OtpType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OtpRequest {
    @NotBlank @Email
    private String email;
    
    @NotBlank
    private String otp;
    
   
    private OtpType type;
}