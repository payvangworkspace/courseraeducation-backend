package com.pv.couseae.Dtos.juspay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionStatus360ResponseDto {

    // ─── Top Level ────────────────────────────────────────────
    @JsonProperty("status")          private String status;          // SUCCESS / FAILURE
    @JsonProperty("responseCode")    private String responseCode;
    @JsonProperty("responseMessage") private String responseMessage;
    @JsonProperty("udfParameters")   private String udfParameters;

    @JsonProperty("payload")
    private PayloadDto payload;

    // ─── Payload ──────────────────────────────────────────────

    @Data @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PayloadDto {

        @JsonProperty("amount")                        private String amount;
        @JsonProperty("bankAccountUniqueId")           private String bankAccountUniqueId;
        @JsonProperty("bankCode")                      private String bankCode;
        @JsonProperty("customResponse")                private String customResponse;
        @JsonProperty("gatewayPayerResponseCode")      private String gatewayPayerResponseCode;
        @JsonProperty("gatewayPayeeResponseCode")      private String gatewayPayeeResponseCode;
        @JsonProperty("gatewayPayerReversalResponseCode") private String gatewayPayerReversalResponseCode;
        @JsonProperty("gatewayPayeeReversalResponseCode") private String gatewayPayeeReversalResponseCode;
        @JsonProperty("gatewayReferenceId")            private String gatewayReferenceId;
        @JsonProperty("gatewayResponseCode")           private String gatewayResponseCode;
        @JsonProperty("gatewayResponseMessage")        private String gatewayResponseMessage;
        @JsonProperty("gatewayResponseStatus")         private String gatewayResponseStatus;
        @JsonProperty("gatewayTransactionId")          private String gatewayTransactionId;
        @JsonProperty("maskedAccountNumber")           private String maskedAccountNumber;
        @JsonProperty("merchantCustomerId")            private String merchantCustomerId;
        @JsonProperty("merchantId")                    private String merchantId;
        @JsonProperty("orgMandateId")                  private String orgMandateId;
        @JsonProperty("payeeMcc")                      private String payeeMcc;
        @JsonProperty("payeeMerchantCustomerId")       private String payeeMerchantCustomerId;
        @JsonProperty("payeeMobileNumber")             private String payeeMobileNumber;
        @JsonProperty("payeeVpa")                      private String payeeVpa;
        @JsonProperty("payerMerchantCustomerId")       private String payerMerchantCustomerId;
        @JsonProperty("payerName")                     private String payerName;
        @JsonProperty("payerVpa")                      private String payerVpa;
        @JsonProperty("payerAccountHash")              private String payerAccountHash;
        @JsonProperty("refUrl")                        private String refUrl;
        @JsonProperty("seqNumber")                     private String seqNumber;
        @JsonProperty("transactionTimestamp")          private String transactionTimestamp;
        @JsonProperty("type")                          private String type;             // e.g. "MERCHANT_CREDITED_VIA_PAY"
        @JsonProperty("umn")                           private String umn;              // e.g. "ABZv1f7d...@bankbiz"
    }
}