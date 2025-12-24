package com.jobsphere.jobsite.service.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChapaService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${chapa.secret.key}")
    private String chapaSecretKey;

    @Value("${chapa.api.url}")
    private String chapaApiUrl;

    @Value("${chapa.callback.url}")
    private String callbackUrl;

    @Value("${chapa.return.url}")
    private String returnUrl;

    /**
     * Initialize a payment with Chapa
     */
    public Map<String, Object> initializePayment(
            BigDecimal amount,
            String currency,
            String email,
            String firstName,
            String lastName,
            String phoneNumber,
            String txRef) {
        try {
            String url = chapaApiUrl + "/transaction/initialize";

            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("amount", amount.toString());
            requestBody.put("currency", currency);
            requestBody.put("email", email);
            requestBody.put("first_name", firstName);
            requestBody.put("last_name", lastName);
            requestBody.put("phone_number", phoneNumber);
            requestBody.put("tx_ref", txRef);
            requestBody.put("callback_url", callbackUrl);

            // Append tx_ref to return_url so frontend knows which payment to verify
            String dynamicReturnUrl = returnUrl + (returnUrl.contains("?") ? "&" : "?") + "tx_ref=" + txRef;
            requestBody.put("return_url", dynamicReturnUrl);
            requestBody.put("customization", Map.of(
                    "title", "EtWorks Payment",
                    "description", "Payment for job posting on EtWorks platform"));

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + chapaSecretKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Make API call
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class);

            // Log raw response for debugging
            log.info("Chapa raw response: {}", response.getBody());

            // Parse response
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());

            Map<String, Object> result = new HashMap<>();

            // Check if response has status field
            if (jsonResponse.has("status")) {
                result.put("status", jsonResponse.get("status").asText());
            }

            if (jsonResponse.has("message")) {
                result.put("message", jsonResponse.get("message").asText());
            }

            if (jsonResponse.has("data") && jsonResponse.get("data") != null) {
                JsonNode data = jsonResponse.get("data");
                if (data.has("checkout_url")) {
                    result.put("checkout_url", data.get("checkout_url").asText());
                }
                if (data.has("tx_ref")) {
                    result.put("tx_ref", data.get("tx_ref").asText());
                }
            }

            result.put("raw_response", response.getBody());

            log.info("Chapa payment initialized successfully for tx_ref: {}", txRef);
            return result;

        } catch (Exception e) {
            log.error("Error initializing Chapa payment: ", e);
            throw new RuntimeException("Failed to initialize payment: " + e.getMessage());
        }
    }

    /**
     * Verify a payment with Chapa
     */
    public Map<String, Object> verifyPayment(String txRef) {
        try {
            String url = chapaApiUrl + "/transaction/verify/" + txRef;

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + chapaSecretKey);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // Make API call
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class);

            // Parse response
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());

            Map<String, Object> result = new HashMap<>();
            result.put("status", jsonResponse.get("status").asText());
            result.put("message", jsonResponse.get("message").asText());

            if (jsonResponse.has("data")) {
                JsonNode data = jsonResponse.get("data");
                result.put("amount", data.get("amount").asText());
                result.put("currency", data.get("currency").asText());
                result.put("status_code", data.get("status").asText());
                result.put("tx_ref", data.get("tx_ref").asText());
                result.put("email", data.get("email").asText());
                result.put("first_name", data.get("first_name").asText());
                result.put("last_name", data.get("last_name").asText());
            }

            result.put("raw_response", response.getBody());

            log.info("Payment verified successfully for tx_ref: {}", txRef);
            return result;

        } catch (Exception e) {
            log.error("Error verifying Chapa payment: ", e);
            throw new RuntimeException("Failed to verify payment: " + e.getMessage());
        }
    }

    /**
     * Generate a unique transaction reference
     */
    public String generateTxRef() {
        return "ETWORKS-" + UUID.randomUUID().toString();
    }
}
