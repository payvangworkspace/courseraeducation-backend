package com.pv.couseae.Dtos.juspay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VpaRequestPayload {
    @JsonProperty("vpa")           private String vpa;
    @JsonProperty("upiRequestId")  private String upiRequestId;
    @JsonProperty("iat")           private String iat;
    @JsonProperty("udfParameters") private String udfParameters;
}
