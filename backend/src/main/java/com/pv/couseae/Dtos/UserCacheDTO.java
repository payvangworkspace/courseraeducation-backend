package com.pv.couseae.Dtos;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCacheDTO implements Serializable {
    // Identity
    private String userId;
    private String fullName;
    private String businessName;
    private String role;
    private String dateOfBirth;
    private String gender;
    private String password;

    // Contact
    private String contactNumber;
    private boolean isContactVerified;
    private boolean isEmailVerified;

    // Account
    private double accountBalance;
    private boolean status;
    private boolean isVerified;

    // Merchant Settings
    private boolean merchantHostedFlag;
    private String processingMode;
    private String appKey;
    private String secretKey;

    // Permissions
    private boolean isPayinEnabled;
    private boolean isPayoutEnabled;
    private boolean isPayinGstEnabled;
    private boolean isPayoutGstEnabled;
    private boolean isAccountNonExpired ;
    private boolean isCrypto;
    private boolean isAccountNonLocked ;
    private boolean isCredentialsNonExpired ;


    // Webhooks
    private String payinWebhookUrl;
    private String payoutWebhookUrl;

    // Flattened DBRef Data
    private Set<String> currencyCode;
    private Set<String> countryCode;
    private Set<String> bankAccounts;

    // Audit
    private LocalDateTime verificationDate;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}