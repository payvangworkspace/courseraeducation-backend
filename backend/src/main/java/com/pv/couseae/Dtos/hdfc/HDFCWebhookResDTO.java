package com.pv.couseae.Dtos.hdfc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HDFCWebhookResDTO {
    @JsonProperty("order_id")
    private String orderId;

    private String signature;

    @JsonProperty("signature_algorithm")
    private String signatureAlgorithm;

    private String status;

    @JsonProperty("status_id")
    private Integer statusId;
}
