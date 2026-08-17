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
 * Geidea callback / webhook payload.
 *
 * Delivery behaviour that changes how this must be handled:
 *
 *  - No callback while an order is InProgress. Authentication failures and 3DS cancellations
 *    mid-journey produce NO callback of their own.
 *  - A customer may retry several times. When the order finally reaches Paid or Failed you get
 *    ONE callback consolidating every attempt under the same orderId, in
 *    transactionStatusHistory. One callback is not one attempt.
 *  - Closing the hosted page produces a callback reporting the transaction was cancelled by
 *    the user.
 *
 * So: handle idempotently on orderId, and write the terminal state rather than incrementing.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeideaCallbackDto {

    private String orderId;
    private String merchantReferenceId;
    private String merchantPublicKey;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String timestamp;
    private String signature;

    private String responseCode;
    private String responseMessage;
    private String detailedResponseCode;
    private String detailedResponseMessage;

    private String tokenId;
    private String reference;

    private List<Map<String, Object>> transactionStatusHistory;

    private final Map<String, Object> additional = new LinkedHashMap<>();

    @JsonAnySetter
    public void put(String key, Object value) {
        additional.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditional() {
        return additional;
    }

    /**
     * Both codes must be "000". Geidea also tells you to re-check the amount against your own
     * order — a valid signature proves the message came from Geidea, not that it matches yours.
     */
    public boolean isPaid() {
        return "000".equals(responseCode) && "000".equals(detailedResponseCode);
    }
}
