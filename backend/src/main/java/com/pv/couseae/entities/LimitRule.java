package com.pv.couseae.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "limit_rules")
public class LimitRule {
    @Id
    private String id;

    private String merchantId;
    private String txnType;
    private String perTxnMin;
    private String perTxnMax;
    private String dailyLimit;
    private String monthlyLimit;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
    private boolean isActive = true;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
