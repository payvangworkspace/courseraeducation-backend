package com.pv.couseae.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "payin_req_Crypto")
public class PayinRequestCrypto {
    @Id
    private String id;
    @Indexed(unique = true)
    private String orderId;              // Internal order ID
    private String cryptoOrderId;              // Internal order ID
    private String merchantId;            // Merchant / user identifier
    private String cryptoType;        // BTC, ETH, USDT, etc.
    private String networkType;      // TRC20, ERC20, BEP20
    private String orderSide;          // BUY / SELL
    private BigDecimal cryptoAmount;      // Crypto quantity
    private BigDecimal fiatAmount;         // INR / USD amount
    private String fiatCurrency;           // INR, USD
    private BigDecimal exchangeRate;       // Rate at time of order
    private String walletAddress;          // Destination / source wallet
    private String merchantConfigId; // Merchant Config data ID
    private String txHash;                 // Blockchain transaction hash
    private String onRampProvider;
    private String paymentLink;
    private String status;      // CREATED, PENDING, SUCCESS, FAILED
    private boolean isReversed;
    private boolean isSettled;
    private String failureReason;
    private String settlementId;            // settlement id from the aggregator
    private BigDecimal charges;
    private BigDecimal gst;
    private BigDecimal netsettlementamount;  // net settlement amount after deducting surcharge and fees
    private LocalDateTime settlementDate;   // when the transaction was settled

    @CreatedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy HH:mm:ss")
    private LocalDateTime createdDate;

    @LastModifiedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy HH:mm:ss")
    private LocalDateTime lastModifiedDate;
}
