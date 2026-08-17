package com.pv.couseae.Dtos.Crypto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoOrderWebhookDto {
    private String event;
    private Merchant merchant;
    private String orderId;
    private String externalOrderId;
    private String referenceId;
    private String invoiceId;
    private String status;
    private String previousStatus;

    private Payment payment;

    private String fiatAmount;
    private String fiatCurrency;
    private String cryptoAmount;
    private String filledAmount;
    private String filledAmountUsd;
    private String exchangeRate;

    private Transaction transaction;

    private String txHash;
    private Integer blockConfirmations;
    private String depositAddress;
    private String customerWalletAddress;

    private String network;
    private String networkDisplayName;
    private String token;
    private String tokenName;

    private String providerOrderId;
    private String onRampProvider;
    private OnRampTransaction onRampTransaction;

    private Customer customer;

    private String createdAt;
    private String updatedAt;
    private String completedAt;
    private String timestamp;

    // ---------------- NESTED CLASSES ----------------
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OnRampTransaction {

        private String providerOrderId;
        private String providerStatus;
        private String provider;

        private BigDecimal fiatAmount;
        private String fiatCurrency;
        private BigDecimal fiatAmountInUsd;

        private BigDecimal cryptoAmount;
        private String cryptoCurrency;
        private String network;

        private BigDecimal conversionPrice;
        private BigDecimal totalFee;

        private String walletAddress;
        private String transactionHash;
        private String transactionLink;
        private String paymentMethod;


        private String providerCreatedAt;

//        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private String providerUpdatedAt;

//        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private String providerCompletedAt;
    }
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Merchant {
        private String id;
        private String externalId;
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Payment {
        private String fiatAmount;
        private String fiatCurrency;
        private String cryptoAmount;
        private String cryptoAmountUsd;
        private String filledAmount;
        private String filledAmountUsd;
        private String exchangeRate;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Transaction {
        private String txHash;
        private Long blockHeight;
        private Integer blockConfirmations;
        private String txTimestamp;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Customer {
        private String id;
        private String email;
    }
}
