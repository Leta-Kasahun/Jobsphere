package com.jobsphere.jobsite.controller.payment;

import com.jobsphere.jobsite.dto.payment.ChapaWebhookPayload;
import com.jobsphere.jobsite.dto.payment.InitiatePaymentRequest;
import com.jobsphere.jobsite.dto.payment.PaymentResponse;
import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Check if user needs to pay for job posting
     */
    @GetMapping("/check-requirement")
    public ResponseEntity<Map<String, Object>> checkPaymentRequirement(
            Authentication authentication) {
        try {
            // Check if user is authenticated
            if (authentication == null || !authentication.isAuthenticated()) {
                log.error("❌ User is not authenticated!");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("needsPayment", false);
                errorResponse.put("error", "User not authenticated");
                return ResponseEntity.status(401).body(errorResponse);
            }

            // Get email from authentication principal
            String email = authentication.getName();
            log.info("🔍 Loading user by email: {}", email);

            // Load the actual User entity from database
            User user = paymentService.getUserByEmail(email);
            if (user == null) {
                log.error("❌ User not found for email: {}", email);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("needsPayment", false);
                errorResponse.put("error", "User not found");
                return ResponseEntity.status(404).body(errorResponse);
            }

            log.info("🔍 Checking payment requirement for user: {}", user.getEmail());

            boolean needsPayment = paymentService.needsPaymentForJobPosting(user);
            log.info("✅ Needs payment: {}", needsPayment);

            long successfulPayments = paymentService.getSuccessfulPaymentCount(user);
            log.info("✅ Successful payments count: {}", successfulPayments);

            BigDecimal price = paymentService.getJobPostingPrice();

            Map<String, Object> response = new HashMap<>();
            response.put("needsPayment", needsPayment);
            response.put("successfulPayments", successfulPayments);
            response.put("price", price);
            response.put("currency", "ETB");
            response.put("message", needsPayment
                    ? "Payment required for job posting"
                    : "First job posting is free");

            log.info("✅ Response: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error checking payment requirement: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("needsPayment", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Initiate a payment
     */
    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(
            Authentication authentication,
            @RequestBody InitiatePaymentRequest request) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new RuntimeException("User not authenticated");
            }

            String email = authentication.getName();
            User user = paymentService.getUserByEmail(email);
            if (user == null) {
                throw new RuntimeException("User not found");
            }

            log.info("Initiating payment for user: {}", user.getEmail());
            PaymentResponse response = paymentService.initiatePayment(user, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error initiating payment: ", e);
            throw e;
        }
    }

    /**
     * Verify a payment
     */
    @GetMapping("/verify/{txRef}")
    public ResponseEntity<PaymentResponse> verifyPayment(
            @PathVariable String txRef) {
        log.info("Verifying payment for tx_ref: {}", txRef);
        PaymentResponse response = paymentService.verifyPayment(txRef);
        return ResponseEntity.ok(response);
    }

    /**
     * Get payment by transaction reference
     */
    @GetMapping("/{txRef}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable String txRef) {
        PaymentResponse response = paymentService.getPaymentByTxRef(txRef);
        return ResponseEntity.ok(response);
    }

    /**
     * Get user's payment history
     */
    @GetMapping("/history")
    public ResponseEntity<Page<PaymentResponse>> getPaymentHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PaymentResponse> payments = paymentService.getUserPayments(
                user,
                PageRequest.of(page, size));
        return ResponseEntity.ok(payments);
    }

    /**
     * Get user's successful payments
     */
    @GetMapping("/successful")
    public ResponseEntity<List<PaymentResponse>> getSuccessfulPayments(
            @AuthenticationPrincipal User user) {
        List<PaymentResponse> payments = paymentService.getUserSuccessfulPayments(user);
        return ResponseEntity.ok(payments);
    }

    /**
     * Webhook endpoint for Chapa payment notifications
     */
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, String>> handleWebhook(
            @RequestBody ChapaWebhookPayload payload) {
        try {
            log.info("Received Chapa webhook: {}", payload);

            // Verify the payment
            if (payload.getData() != null && payload.getData().getTx_ref() != null) {
                paymentService.verifyPayment(payload.getData().getTx_ref());
            }

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Webhook processed successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing webhook: ", e);
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
