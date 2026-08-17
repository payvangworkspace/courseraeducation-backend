package com.pv.couseae.Dtos.juspay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionStatus360RequestDto {

    @JsonProperty("merchantRequestId")    private String merchantRequestId;    // e.g. "ORDMxNameTransaction170357165578"
    @JsonProperty("transactionType")      private String transactionType;      // e.g. "MERCHANT_CREDITED_VIA_PAY"
    @JsonProperty("transactionTimestamp") private String transactionTimestamp; // e.g. "2016-11-25T00:00:00+05:30"
    @JsonProperty("iat")                  private String iat;                  // set by client
    @JsonProperty("udfParameters")        private String udfParameters;        // e.g. "{}"
}
