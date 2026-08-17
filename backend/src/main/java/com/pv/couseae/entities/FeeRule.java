package com.pv.couseae.entities;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "fee_rules")
public class FeeRule {

    @Id
    private String ruleId;

    // Nullable for global rules
    private String merchantId;

    // e.g. PAYIN, PAYOUT, REFUND, ADJUSTMENT
    private String txnType;

    // FLAT, PERCENT, MIXED
    private String feeType;

    // If FLAT => currency amount, If PERCENT => percentage value
    private BigDecimal feeValue;

    // Minimum cap applied on fee
    private BigDecimal capMin;

    // Maximum cap applied on fee
    private BigDecimal capMax;

    // Commission % for partner/agent
    private BigDecimal commissionPercent;

    private LocalDateTime effectiveFrom;

    private LocalDateTime effectiveTo;
    private boolean isActive = true;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

}
