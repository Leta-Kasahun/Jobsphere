package com.jobsphere.jobsite.model.payment;

import com.jobsphere.jobsite.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String txRef; // Transaction reference (unique identifier)

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency; // ETB

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentPurpose purpose; // JOB_POSTING, SUBSCRIPTION, etc.

    private String chapaCheckoutUrl; // URL to redirect user for payment

    private String chapaTransactionId; // Chapa's transaction ID

    @Column(columnDefinition = "TEXT")
    private String chapaResponse; // Full response from Chapa for debugging

    private String email;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    @Column(columnDefinition = "TEXT")
    private String metadata; // Additional data (job details, etc.)

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime paidAt;

    private LocalDateTime verifiedAt;
}
