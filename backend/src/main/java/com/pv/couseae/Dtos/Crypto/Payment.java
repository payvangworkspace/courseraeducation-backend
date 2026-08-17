package com.pv.couseae.Dtos.Crypto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Optional;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Payment {

    private BigDecimal fiatAmount;
    private String fiatCurrency;

    private Optional<BigDecimal> cryptoAmount = Optional.empty();
    private Optional<BigDecimal> cryptoAmountUsd = Optional.empty();
    private Optional<BigDecimal> filledAmount = Optional.empty();
    private Optional<BigDecimal> filledAmountUsd = Optional.empty();
    private Optional<BigDecimal> exchangeRate = Optional.empty();
}