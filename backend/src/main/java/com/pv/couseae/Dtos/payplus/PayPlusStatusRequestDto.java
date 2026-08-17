package com.pv.couseae.Dtos.payplus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayPlusStatusRequestDto {
    private String merchantOrderId;
}