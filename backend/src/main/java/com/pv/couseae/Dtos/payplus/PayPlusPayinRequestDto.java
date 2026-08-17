package com.pv.couseae.Dtos.payplus;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PayPlusPayinRequestDto {

    private double amount;
    private String merchantOrderId;
    private String username;
    private CustomerMeta customerMeta;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerMeta {
        private String playerId;
    }
}