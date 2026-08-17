package com.pv.couseae.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "crypto_config")
public class MerchantCryptoConfig implements Serializable {
    @Id
    private String id;

    // Merchant Mapping
    private String merchantId;   // userId / email
    private String fiatCurrencyCode;  // INR , USD,
    // Crypto Settings
    private boolean cryptoEnabled;
//    private Set<String> supportedCoins;     // BTC, ETH, USDT, etc.
    private String defaultCoin; // BTC, ETH, USDT, etc.
    private String coinName;

    // Network Settings
//    private Set<String> networks;            // ERC20, TRC20, BEP20
    private String defaultNetwork;
    private String networkCurrencyCode;

    // Wallet / Provider
    private String walletProvider;           // BINANCE, COINBASE, INTERNAL
    private String walletAddress;
    private boolean isWalletVerified;
//    private String apiKey;
//    private String secretKey;

    // Limits
    private double minAmount;
    private double maxAmount;

    // Fee Config
    private boolean feeEnabled;
    private double flatFee;
    private double percentageFee;

    // Webhooks
    private String cryptoWebhookUrl;

    // Status
    private boolean status;
    private boolean isTestMode;

    // Audit
    @CreatedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy HH:mm:ss")
    private LocalDateTime createdDate;

    @LastModifiedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy HH:mm:ss")
    private LocalDateTime lastModifiedDate;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String lastModifiedBy;

}
