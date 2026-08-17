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
public class JwsEnvelopeDto {
    @JsonProperty("protected")
    private String protectedValue;
    private String payload;
    private String signature;
}
