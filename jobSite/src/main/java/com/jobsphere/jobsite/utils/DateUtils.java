package com.jobsphere.jobsite.utils;

import java.time.Instant;

public class DateUtils {
    private DateUtils() {}
    
    public static Instant now() {
        return Instant.now();
    }
    
    public static Instant addMinutes(Instant time, int minutes) {
        return time.plusSeconds(minutes * 60L);
    }
    
    public static boolean isExpired(Instant expiryTime) {
        return now().isAfter(expiryTime);
    }
}