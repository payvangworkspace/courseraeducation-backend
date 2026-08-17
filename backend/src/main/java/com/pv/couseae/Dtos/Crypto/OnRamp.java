package com.pv.couseae.Dtos.Crypto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Optional;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OnRamp {

    private String provider;
    private String providerName;
    private String widgetLinkUrl;

    private Optional<String> providerOrderId = Optional.empty();
}