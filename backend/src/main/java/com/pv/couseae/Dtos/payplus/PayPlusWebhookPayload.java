package com.pv.couseae.Dtos.payplus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayPlusWebhookPayload {

    private String event;             // "payin.success"
    private String status;            // "success" | "processing" | "failed"
    private String orderId;           // PayPlus order id
    private String payoutId;          // payout events only
    private String merchantOrderId;   // your original orderId

    private String amount;            // original order amount
    private String requestedAmount;
    private String payableAmount;     // shown-to-pay (after offset)
    private Integer amountOffsetPaise;// paise shaved for uniqueness
    private String receivedAmount;    // actually received
    private String fee;
    private String netAmount;         // credited to you
    private String utr;               // bank UTR (null until settled)

    private String timestamp;
}