package com.pv.couseae.Dtos.juspay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VerifyVpaResponseDto {
    private String vpa;
    private String status;
    private String responseCode;
    private String responseMessage;
    private String gatewayResponseCode;
    private String gatewayResponseMessage;
    private String gatewayResponseStatus;
    private String gatewayTransactionId;
    private String isMerchant;
    private String isMerchantVerified;
    private String merchantId;
    private String merchantChannelId;
    private String mcc;

    @JsonIgnoreProperties(ignoreUnknown = true)
    private Object merchantType;   // ← nested object, ignore for now
}