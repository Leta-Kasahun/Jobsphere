package com.jobsphere.jobsite.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitiatePaymentRequest {
    private BigDecimal amount;
    private String currency; // ETB
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String purpose; // JOB_POSTING, SUBSCRIPTION, etc.
    private String metadata; // Additional data (JSON string)
}
