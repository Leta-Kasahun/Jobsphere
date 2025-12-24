package com.jobsphere.jobsite.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapaWebhookPayload {
    private String event;
    private Data data;

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private String tx_ref;
        private String status;
        private String amount;
        private String currency;
        private String email;
        private String first_name;
        private String last_name;
        private String phone_number;
        private String created_at;
        private String updated_at;
    }
}
