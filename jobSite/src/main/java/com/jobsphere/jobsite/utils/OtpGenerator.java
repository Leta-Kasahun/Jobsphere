package com.jobsphere.jobsite.utils;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

@Component
public class OtpGenerator {
    private static final String NUMBERS = "0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    
    public String generateSixDigitOtp() {
        StringBuilder otp = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            otp.append(NUMBERS.charAt(RANDOM.nextInt(NUMBERS.length())));
        }
        return otp.toString();
    }
}