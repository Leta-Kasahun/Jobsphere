package com.jobsphere.jobsite.model.payment;

public enum PaymentStatus {
    PENDING, // Payment initiated but not completed
    PROCESSING, // Payment is being processed
    SUCCESS, // Payment successful
    FAILED, // Payment failed
    CANCELLED, // Payment cancelled by user
    EXPIRED, // Payment link expired
    VERIFIED // Payment verified by webhook
}
