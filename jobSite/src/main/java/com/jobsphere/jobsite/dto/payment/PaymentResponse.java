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
public class PaymentResponse {
    private Long id;
    private String txRef;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String purpose;
    private String checkoutUrl;
    private String createdAt;
    private String paidAt;
}
