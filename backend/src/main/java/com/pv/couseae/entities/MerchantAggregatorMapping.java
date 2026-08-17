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
@Document(collection = "merchant_aggregator_mapping")
public class MerchantAggregatorMapping {
    @Id
    private String id;
    private String merchantId;          // Reference to User._id
    private String aggregatorCode;     // Reference to ApiMaster._id
    private String environment;     // e.g. UAT, PROD (override if needed)
    private String aliasName;       // Optional label like "HDFC-UAT"
    private String txnType;     // PAYIN or PAYOUT
    private Integer priority;         // e.g., HIGH, MEDIUM, LOW, or numeric "1", "2", "3"
    private boolean active;         // User-specific enable/disable
    private String createdBy;
    private LocalDateTime createdOn;
    private String updatedBy;
    private LocalDateTime updatedOn;
}
