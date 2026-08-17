package com.pv.couseae.Dtos.juspay;

import lombok.Data;

@Data
public class RegisterJusPayIntentResponse {
    private String status;          // SUCCESS | FAILURE
    private String responseCode;
    private String responseMessage;
    private Payload payload;

    @Data
    public static class Payload {
        private String merchantId;
        private String merchantChannelId;
        private String gatewayTransactionId;  // → tid in deeplink
        private String orderId;               // → tr  in deeplink
        private String payeeVpa;              // → pa  in deeplink
        private String payeeName;             // → pn  in deeplink
        private String payeeMcc;             // → mc  in deeplink
        private String amount;               // → am  in deeplink
        private String currency;             // → cu  in deeplink
        private String remarks;              // → tn  in deeplink
        private String refUrl;               // → url in deeplink
        private String merchantRequestId;
        private String udfParameters;
    }
}