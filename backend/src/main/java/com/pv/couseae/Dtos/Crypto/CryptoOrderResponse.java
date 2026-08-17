package com.pv.couseae.Dtos.Crypto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoOrderResponse {

    private CryptoOrderData data;
    private String message;
    private int status;
}