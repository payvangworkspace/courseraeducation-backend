package com.pv.couseae.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionFeeReq {
    private String merchantId;
    private String userId;       // optional, if you track per-user
    private String txnType;
    private BigDecimal txnAmount;
}
