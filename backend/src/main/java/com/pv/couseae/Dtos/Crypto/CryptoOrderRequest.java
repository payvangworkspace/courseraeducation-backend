package com.pv.couseae.Dtos.Crypto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CryptoOrderRequest {
    private String externalOrderId;
    private String email;
    private BigDecimal fiatAmount;
    private String fiatCurrency;
    private String networkName;
    private String tokenSymbol;
    private String referenceId;
    private String invoiceId;
    private String firstName;
    private String lastName;
    private Integer sessionExpiryMinutes;
}