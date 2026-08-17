package com.pv.couseae.Dtos.Crypto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebCollect360ResponseDto {

    // ─── Top Level ────────────────────────────────────────────
    @JsonProperty("status")          private String status;           // SUCCESS / FAILURE
    @JsonProperty("responseCode")    private String responseCode;     // e.g. "SUCCESS"
    @JsonProperty("responseMessage") private String responseMessage;  // e.g. "SUCCESS"
    @JsonProperty("udfParameters")   private String udfParameters;    // e.g. "{}"

    @JsonProperty("payload")
    private PayloadDto payload;

    // ─── Payload ──────────────────────────────────────────────

    @Data @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PayloadDto {

        @JsonProperty("merchantId")             private String merchantId;              // e.g. "SALORAUAT"
        @JsonProperty("merchantChannelId")      private String merchantChannelId;       // e.g. "SALORAUATAPP"
        @JsonProperty("merchantRequestId")      private String merchantRequestId;       // e.g. "ORD123456789"
        @JsonProperty("gatewayTransactionId")   private String gatewayTransactionId;    // e.g. "YJP32bf37..."
        @JsonProperty("gatewayReferenceId")     private String gatewayReferenceId;      // e.g. "608301143516"
        @JsonProperty("gatewayResponseStatus")  private String gatewayResponseStatus;   // e.g. "SUCCESS"
        @JsonProperty("gatewayResponseCode")    private String gatewayResponseCode;     // e.g. "00"
        @JsonProperty("gatewayResponseMessage") private String gatewayResponseMessage;  // e.g. "Collect request sent successfully"
        @JsonProperty("payerVpa")               private String payerVpa;                // e.g. "9361415243@ypay"
        @JsonProperty("payeeVpa")               private String payeeVpa;                // e.g. "salorauat@ypay"
        @JsonProperty("payeeMcc")               private String payeeMcc;                // e.g. "7322"
        @JsonProperty("amount")                 private String amount;                  // e.g. "20.00"
        @JsonProperty("remarks")                private String remarks;                 // e.g. "Order payment"
        @JsonProperty("refUrl")                 private String refUrl;                  // e.g. "https://..."
        @JsonProperty("transactionTimestamp")   private String transactionTimestamp;    // e.g. "2026-03-24T13:01:34+05:30"
        @JsonProperty("expiryTimestamp")        private String expiryTimestamp;         // e.g. "2026-03-24T13:11:34+05:30"
    }
}