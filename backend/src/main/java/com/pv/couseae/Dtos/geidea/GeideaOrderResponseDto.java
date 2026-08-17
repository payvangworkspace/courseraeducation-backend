package com.pv.couseae.Dtos.geidea;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared response shape for order status and refund calls.
 *
 * Field names are taken from the callback payload, which mirrors the order object. Verify
 * against a real sandbox response and adjust — the "additional" map will show anything that
 * did not map, so run one call and check the logs rather than assuming.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeideaOrderResponseDto {

    private Order order;

    private String responseCode;
    private String responseMessage;
    private String detailedResponseCode;
    private String detailedResponseMessage;

    private final Map<String, Object> additional = new LinkedHashMap<>();

    @JsonAnySetter
    public void put(String key, Object value) {
        additional.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditional() {
        return additional;
    }

    public boolean isSuccess() {
        return "000".equals(responseCode) && "000".equals(detailedResponseCode);
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Order {
        private String orderId;
        private String merchantReferenceId;
        private BigDecimal amount;
        private String currency;
        private String status;
        private String detailedStatus;
        private String createdDate;
        private String updatedDate;
        private String paymentMethod;
        private String tokenId;

        private List<Map<String, Object>> transactions;

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
