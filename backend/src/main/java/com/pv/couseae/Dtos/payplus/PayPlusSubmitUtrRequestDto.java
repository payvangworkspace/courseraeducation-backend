package com.pv.couseae.Dtos.payplus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayPlusSubmitUtrRequestDto {
    private String orderId;
    private String utr;
}