package com.pv.couseae.Dtos.juspay;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterJusPayIntentRequest {
    private String merchantRequestId;   // unique order ID (max 35 chars)
    private String amount;              // "1.00" — always 2 decimals
    private String flow;                // "TRANSACTION"
    private String remarks;             // optional, max 50 chars
    private String refUrl;              // optional
    private String refCategory;         // "01" ad / "02" invoice
    private String iat;                 // 13-digit epoch millis
    private String intentRequestExpiryMinutes; // e.g. "10"
    private String udfParameters;       // "{\"udf1\":\"val\"}"
}
