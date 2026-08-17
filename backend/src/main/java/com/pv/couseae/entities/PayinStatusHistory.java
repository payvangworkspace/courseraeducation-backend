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
@Document(collection = "payin_status_history")
public class PayinStatusHistory {
    @Id
    private String id;
    private String merchantId;
    private String orderId; // PAYIN / PAYOUT / ADJUSTMENT / REFUND
    private BigDecimal amount;
    private String status; // INITIATED, PENDING, SUCCESS, FAILED
    private String remarks;// Narration for transaction
    private LocalDateTime createdAt;
}
