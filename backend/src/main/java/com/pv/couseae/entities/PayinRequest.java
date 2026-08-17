package com.pv.couseae.entities;

import com.pv.couseae.enums.TransactionStatus;
import com.pv.couseae.enums.TransactionTypes;
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
@Document(collection = "payin_requests")
public class PayinRequest {

    @Id
    private String orderId;

    // Merchant Details
    private String merchantId;
    private String aggregatorCode;
    private String aggregatorid;

    // HDFC / Juspay Order Details
    private String aggregatorTxnId;      // ordeh_xxxxx
    private String customerId;
    private String hdfcOrderId;
    private String gatewayReferenceId;

    // Transaction Details
    private String txnId;
    private String txnUuid;
    private String payment_id;
    private String paymentMethod;
    private String paymentMethodType;
    private String paymentMode;
    private String authType;

    // Amount Details
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private String currency;
    private String surcharge;
    private Double effectiveAmount;
    private Double amountRefunded;
    private Boolean refunded;

    // Customer Details
    private String firstName;
    private String lastName;
    private String customerEmail;
    private String customerMobile;
    private String payerVpa;

    // Card Details
    private String expiryYear;
    private String expiryMonth;
    private String cardReference;
    private Boolean savedToLocker;
    private String cardHolderName;
    private String cardIssuer;
    private String cardIssuerCountry;
    private String cardLastFourDigits;
    private Boolean usingSavedCard;
    private String cardFingerprint;
    private String cardIsin;
    private String cardType;
    private String cardBrand;
    private String extendedCardType;
    private String cardSubTypeCategory;
    private String paymentAccountReference;
    private String juspayBankCode;

    // Legacy Card Fields
    private String card_number;
    private String card_category;

    // Gateway Response
    private String bank_ref_num;
    private String rrn;
    private String authCode;
    private String gatewayTxnId;
    private String gatewayRespCode;
    private String gatewayRespMessage;
    private String gatewayMerchantId;
    private String pg_bankcode;

    // Transaction Status
    private String transactionStatus =
            TransactionStatus.INITIATED.toString();

    private String transactionType =
            TransactionTypes.ORDER.toString();

    private String statusMessage;
    private String responseCode;
    private String bankRemarks;
    private String failedReason;

    // Payment Metadata
    private String paymentdesc;
    private String payment_date;
    private String orderExpiry;

    // URLs
    private String callbackUrl;
    private String returnUrl;
    private String paymentLink;

    // Retry & Callback
    private boolean callbackSent;
    private Integer retryCount;
    private String retryReason;

    // UDF Fields
    private String udf1;
    private String udf2;
    private String udf3;
    private String udf4;
    private String udf5;

    // Audit & Tracking
    private String rawResponse;
    private String ipAddress;
    private String createdBy;

    // Timestamps
    private LocalDateTime initiatedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime successAt;
    private LocalDateTime failedAt;
    private LocalDateTime createdOn;
    private LocalDateTime linkExpiryTime;

    // Settlement Details
    private String settlementId;
    private BigDecimal charges;
    private BigDecimal gst;
    private BigDecimal netsettlementamount;
    private boolean settled;
    private LocalDateTime settlementDate;
}