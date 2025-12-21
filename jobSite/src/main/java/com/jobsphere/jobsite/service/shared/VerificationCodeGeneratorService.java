package com.jobsphere.jobsite.service.shared;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class VerificationCodeGeneratorService {
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateSixDigitCode() {
        int code = 100000 + RANDOM.nextInt(900000); // Generates number between 100000-999999
        return String.valueOf(code);
    }
}
