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
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "payin_settlements")
public class PayinSettlement {

    @Id
    private String settlementId;     // Primary Key: SETT12345

    private String merchantId;          // merchantId

    private String currency;         // INR, AED, etc.

    private BigDecimal settledAmount;  // Final settled value
    private BigDecimal charges;        // PG or Aggregator charges
    private BigDecimal netAmount;      // amount - charges

    private String utr;              // Bank reference UTR number

    private String settlementStatus; // SUCCESS / FAILED / PENDING
    private String settlementReason; // Reason for settlement

    private String paymentMode;      // Bank transfer / NETBANKING / UPI / Wallet/ etc.
    private String upiId;
    private String baneficiaryName;    // Name of beneficiaries
    private String accountNumber;     // Bank account number
    private String ifscCode;          // Bank IFSC code
    private LocalDateTime settlementDate;  // When settlement completed

    private String batchId;          // Batch-wise settlement grouping

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
