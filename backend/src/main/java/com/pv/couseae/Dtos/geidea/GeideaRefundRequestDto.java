package com.pv.couseae.Dtos.geidea;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Geidea refund request. Pass the full order amount for a full refund, a lesser amount for a
 * partial one. Currency must match the original order.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeideaRefundRequestDto {

    /** The Geidea orderId, not your merchantReferenceId. */
    private String orderId;

    private BigDecimal amount;

    private String currency;
}
