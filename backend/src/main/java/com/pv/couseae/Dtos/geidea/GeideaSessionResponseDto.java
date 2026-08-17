package com.pv.couseae.Dtos.geidea;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Geidea Create Session response.
 *
 * Unknown fields are captured into {@code additional} instead of being discarded. A bare
 * ignoreUnknown swallows fields the acquirer adds or renames with nothing failing — the same
 * way PayPlus webhook fields went missing silently.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeideaSessionResponseDto {

    private Session session;

    private String responseCode;
    private String responseMessage;
    private String detailedResponseCode;
    private String detailedResponseMessage;
    private String language;
    private String signature;

    private final Map<String, Object> additional = new LinkedHashMap<>();

    @JsonAnySetter
    public void put(String key, Object value) {
        additional.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditional() {
        return additional;
    }

    /** Geidea signals success with "000" on BOTH codes. Either alone is not enough. */
    public boolean isSuccess() {
        return "000".equals(responseCode) && "000".equals(detailedResponseCode);
    }

    public String sessionId() {
        return session == null ? null : session.getId();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Session {
        private String id;
        private BigDecimal amount;
        private String currency;
        private String status;
        private String callbackUrl;
        private String returnUrl;
        private String expiryDate;
        private String merchantId;
        private String merchantPublicKey;
        private String merchantReferenceId;
        private String language;
        private String paymentOperation;
        private Boolean cardOnFile;
        private String tokenId;
        private String paymentIntentId;

        private final Map<String, Object> additional = new LinkedHashMap<>();

        @JsonAnySetter
        public void put(String key, Object value) {
            additional.put(key, value);
        }

        @JsonAnyGetter
        public Map<String, Object> getAdditional() {
            return additional;
        }
    }
}
