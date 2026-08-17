package com.pv.couseae.Dtos.Crypto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.Instant;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoOrderData {

    private String id;
    private String externalOrderId;
    private String referenceId;
    private String invoiceId;
    private String status;

    private Customer customer;
    private Payment payment;
    private Crypto crypto;
    private OnRamp onRamp;
    private Transaction transaction;

    private String paymentLinkUrl;
    private Instant sessionExpiresAt;

    private String webhookStatus;
    private boolean parentNotified;

    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt; // nullable
}