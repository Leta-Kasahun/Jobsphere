package com.jobsphere.jobsite.service.payment;

import com.jobsphere.jobsite.dto.payment.InitiatePaymentRequest;
import com.jobsphere.jobsite.dto.payment.PaymentResponse;
import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.model.payment.Payment;
import com.jobsphere.jobsite.model.payment.PaymentPurpose;
import com.jobsphere.jobsite.model.payment.PaymentStatus;
import com.jobsphere.jobsite.repository.UserRepository;
import com.jobsphere.jobsite.repository.payment.PaymentRepository;
import com.jobsphere.jobsite.repository.job.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobsphere.jobsite.dto.job.JobCreateRequest;
import com.jobsphere.jobsite.dto.job.JobResponse;
import com.jobsphere.jobsite.service.job.JobService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ChapaService chapaService;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobService jobService;
    private final ObjectMapper objectMapper;

    @Value("${job.posting.price}")
    private BigDecimal jobPostingPrice;

    @Value("${job.posting.free.limit}")
    private int freeJobLimit;

    /**
     * Check if user needs to pay for job posting
     */
    public boolean needsPaymentForJobPosting(User user) {
        try {
            log.info("🔍 Checking payment requirement for user: {}", user.getEmail());

            // Get total jobs posted by this user
            long totalJobsPosted = getTotalJobsPosted(user);
            log.info("📊 Total jobs posted: {}", totalJobsPosted);

            // Get successful payments count
            long successfulPayments = getSuccessfulPaymentCount(user);
            log.info("💳 Successful payments: {}", successfulPayments);

            // If total jobs posted is less than free limit, no payment needed
            if (totalJobsPosted < freeJobLimit) {
                log.info("✅ User has {}/{} free jobs remaining - no payment needed",
                        (freeJobLimit - totalJobsPosted), freeJobLimit);
                return false;
            }

            // User has exceeded free limit
            // Check if they have enough paid job slots
            long paidJobsAllowed = successfulPayments;
            long paidJobsUsed = totalJobsPosted - freeJobLimit;

            boolean needsPayment = paidJobsUsed >= paidJobsAllowed;

            log.info("📈 Paid jobs used: {}, Paid jobs allowed: {}, Needs payment: {}",
                    paidJobsUsed, paidJobsAllowed, needsPayment);

            return needsPayment;

        } catch (Exception e) {
            log.error("❌ Error checking payment requirement: ", e);
            // In case of error, require payment to be safe
            return true;
        }
    }

    /**
     * Get the count of successful payments for a user
     */
    public long getSuccessfulPaymentCount(User user) {
        try {
            return paymentRepository.countSuccessfulPaymentsByUser(user);
        } catch (Exception e) {
            log.error("Error counting successful payments for user {}: ", user.getEmail(), e);
            return 0;
        }
    }

    /**
     * Get total jobs posted by user
     */
    public long getTotalJobsPosted(User user) {
        try {
            return jobRepository.countByCompanyProfileUserId(user.getId());
        } catch (Exception e) {
            log.error("Error counting total jobs for user {}: ", user.getEmail(), e);
            return 0;
        }
    }

    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email).orElse(null);
    }

    /**
     * Initiate a payment
     */
    @Transactional
    public PaymentResponse initiatePayment(User user, InitiatePaymentRequest request) {
        try {
            // Generate transaction reference
            String txRef = chapaService.generateTxRef();

            // Create payment record
            Payment payment = Payment.builder()
                    .user(user)
                    .txRef(txRef)
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .status(PaymentStatus.PENDING)
                    .purpose(PaymentPurpose.valueOf(request.getPurpose()))
                    .email(request.getEmail())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phoneNumber(request.getPhoneNumber())
                    .metadata(request.getMetadata())
                    .build();

            payment = paymentRepository.save(payment);

            // Initialize payment with Chapa
            Map<String, Object> chapaResponse = chapaService.initializePayment(
                    request.getAmount(),
                    request.getCurrency(),
                    request.getEmail(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getPhoneNumber(),
                    txRef);

            // Update payment with Chapa response
            payment.setChapaCheckoutUrl((String) chapaResponse.get("checkout_url"));
            payment.setChapaResponse((String) chapaResponse.get("raw_response"));
            payment.setStatus(PaymentStatus.PROCESSING);
            payment = paymentRepository.save(payment);

            return mapToResponse(payment);

        } catch (Exception e) {
            log.error("Error initiating payment: ", e);
            throw new RuntimeException("Failed to initiate payment: " + e.getMessage());
        }
    }

    /**
     * Verify a payment
     */
    @Transactional
    public PaymentResponse verifyPayment(String txRef) {
        Payment payment = paymentRepository.findByTxRef(txRef)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        try {
            log.info("🔍 Starting verification for txRef: {}", txRef);
            // Verify with Chapa
            Map<String, Object> chapaResponse = chapaService.verifyPayment(txRef);
            log.info("📡 Chapa Response: {}", chapaResponse);

            String statusCode = (String) chapaResponse.get("status_code");
            log.info("💹 Status Code from Chapa: {}", statusCode);

            if ("success".equalsIgnoreCase(statusCode)) {
                log.info("✅ Payment success confirmed for txRef: {}", txRef);
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setPaidAt(LocalDateTime.now());
                payment.setVerifiedAt(LocalDateTime.now());
                payment = paymentRepository.save(payment);

                // Automatic Job Posting Logic
                String metadata = payment.getMetadata();
                log.info("📂 Payment Metadata: {}", metadata);

                if (payment.getPurpose() == PaymentPurpose.JOB_POSTING && metadata != null && !metadata.isEmpty()) {
                    try {
                        log.info("🚀 Purpose is JOB_POSTING, attempting automatic post...");
                        JsonNode metadataNode = objectMapper.readTree(metadata);
                        log.info("Parsed Metadata: {}", metadataNode);

                        if (metadataNode.has("jobData")) {
                            log.info("📝 Job data found in metadata, posting job automatically for user: {}",
                                    payment.getUser().getId());
                            JsonNode jobData = metadataNode.get("jobData");

                            // Use a copy of the mapper that ignores unknown properties to avoid crashes
                            ObjectMapper lenientMapper = objectMapper.copy()
                                    .configure(
                                            com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                                            false);

                            JobCreateRequest jobRequest = lenientMapper.treeToValue(jobData, JobCreateRequest.class);
                            log.info("📋 Deserialized Job Request: {}", jobRequest);

                            JobResponse postedJob = jobService.createJobForUser(jobRequest, payment.getUser().getId(),
                                    true);
                            log.info("🎉 Job automatically posted with ID: {}", postedJob.id());
                        } else {
                            log.warn("⚠️ Metadata does not contain 'jobData' key!");
                        }
                    } catch (Exception e) {
                        log.error("❌ Failed to automatically post job after successful payment. Error: {}",
                                e.getMessage(), e);
                    }
                } else {
                    log.warn("⚠️ Automatic posting skipped: Purpose={}, Metadata present={}",
                            payment.getPurpose(), metadata != null);
                }
            } else {
                log.warn("❌ Payment verification failed or pending. Status: {}", statusCode);
                payment.setStatus(PaymentStatus.FAILED);
                payment = paymentRepository.save(payment);
            }

            return mapToResponse(payment);

        } catch (Exception e) {
            log.error("Error verifying payment: ", e);
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new RuntimeException("Failed to verify payment: " + e.getMessage());
        }
    }

    /**
     * Get payment by transaction reference
     */
    public PaymentResponse getPaymentByTxRef(String txRef) {
        Payment payment = paymentRepository.findByTxRef(txRef)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return mapToResponse(payment);
    }

    /**
     * Get user's payment history
     */
    public Page<PaymentResponse> getUserPayments(User user, Pageable pageable) {
        return paymentRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get user's successful payments
     */
    public List<PaymentResponse> getUserSuccessfulPayments(User user) {
        return paymentRepository.findSuccessfulPaymentsByUser(user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map Payment entity to PaymentResponse DTO
     */
    private PaymentResponse mapToResponse(Payment payment) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        return PaymentResponse.builder()
                .id(payment.getId())
                .txRef(payment.getTxRef())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus().name())
                .purpose(payment.getPurpose().name())
                .checkoutUrl(payment.getChapaCheckoutUrl())
                .createdAt(payment.getCreatedAt().format(formatter))
                .paidAt(payment.getPaidAt() != null ? payment.getPaidAt().format(formatter) : null)
                .build();
    }

    /**
     * Get job posting price
     */
    public BigDecimal getJobPostingPrice() {
        return jobPostingPrice;
    }
}
