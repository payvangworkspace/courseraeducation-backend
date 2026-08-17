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
public class PayPlusStatusResponseDto {

    private boolean success;
    private String message;
    private StatusData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StatusData {
        private String orderId;
        private String merchantOrderId;
        private String amount;
        private String status;
        private String utr;
    }
}