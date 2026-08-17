package com.pv.couseae.Dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HdfcSessionResponseDTO {
    private String status;
    private String id;
    private String order_id;
    private PaymentLinks payment_links;
    private SdkPayload sdk_payload;
    private ZonedDateTime order_expiry;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentLinks {
        private String web;
        private ZonedDateTime expiry;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SdkPayload {
        private String requestId;
        private String service;
        private Payload payload;
        private ZonedDateTime expiry;
        private ZonedDateTime currTime;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Payload {
            private String firstName;
            private String clientId;
            private String customerId;
            private String displayBusinessAs;
            private String orderId;
            private String returnUrl;
            private String currency;
            private String customerEmail;
            private String customerPhone;
            private String service;
            private String description;
            private String environment;
            private String lastName;
            private String merchantId;
            private String amount;
            private ZonedDateTime clientAuthTokenExpiry;
            private String clientAuthToken;
            private String action;
            private boolean collectAvsInfo;
        }
    }
}
