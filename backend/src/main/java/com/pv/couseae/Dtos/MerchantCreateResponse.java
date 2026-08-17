package com.pv.couseae.Dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MerchantCreateResponse {

    private DataNode data;     // will be null on error
    private String message;
    private int status;
    private String error;      // present for 4xx / 5xx

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataNode {
        private String merchantId;
        private String apiKey;
        private String apiSecret;
        private String name;
        private String email;
    }
}