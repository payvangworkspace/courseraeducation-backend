package com.pv.couseae.Dtos.payplus;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayPlusPayinResponseDto {

    private boolean success;
    private String message;
    private Data data;

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {

        private String orderId;
        private String merchantOrderId;
        private String paymentUrl;
        private String txnId;
        private String payableAmount;
        private String status;

        @JsonProperty("expiresAt")
        private String expiresAt;

        private UpiIntent upiIntent;    // was missing entirely
    }

    @lombok.Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UpiIntent {
        private Map<String, String> apps;   // paytm / cred / phonepe / bharatpe
    }
}