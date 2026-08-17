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
public class JusPayCollectWebhookDto {

    @JsonProperty("amount")                 private String amount;                  // e.g. "100.00"
    @JsonProperty("customResponse")         private String customResponse;          // e.g. "{}"
    @JsonProperty("expiry")                 private String expiry;                  // e.g. "2026-03-26T11:33:08+05:30"
    @JsonProperty("gatewayReferenceId")     private String gatewayReferenceId;      // e.g. "608501148887"
    @JsonProperty("gatewayResponseCode")    private String gatewayResponseCode;     // e.g. "U69"
    @JsonProperty("gatewayResponseMessage") private String gatewayResponseMessage;  // e.g. "Your collect request has expired"
    @JsonProperty("gatewayResponseStatus")  private String gatewayResponseStatus;   // SUCCESS / EXPIRED / FAILED
    @JsonProperty("gatewayTransactionId")   private String gatewayTransactionId;    // e.g. "YJP2ae9d83..."
    @JsonProperty("merchantChannelId")      private String merchantChannelId;       // e.g. "SALORAUATAPP"
    @JsonProperty("merchantId")             private String merchantId;              // e.g. "SALORAUAT"
    @JsonProperty("merchantRequestId")      private String merchantRequestId;       // e.g. "ORD0551260018"
    @JsonProperty("payeeMcc")               private String payeeMcc;               // e.g. "7322"
    @JsonProperty("payeeVpa")               private String payeeVpa;               // e.g. "salorauat@ypay"
    @JsonProperty("payerName")              private String payerName;               // e.g. "Devendra Sharma"
    @JsonProperty("payerVpa")               private String payerVpa;               // e.g. "7498235821@ypay"
    @JsonProperty("refUrl")                 private String refUrl;
    @JsonProperty("transactionTimestamp")   private String transactionTimestamp;    // e.g. "2026-03-26T11:23:08+05:30"
    @JsonProperty("type")                   private String type;                    // e.g. "MERCHANT_CREDITED_VIA_COLLECT"
    @JsonProperty("udfParameters")          private String udfParameters;           // e.g. "{}"
}