package com.pv.couseae.mappers;

import com.pv.couseae.Dtos.UserCacheDTO;
import com.pv.couseae.entities.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.stream.Collectors;

public class UserCacheMapper {

    public static UserCacheDTO toDto(User user) {
        if (user == null) return null;

        return UserCacheDTO.builder()

                // Identity
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .businessName(user.getBusinessName())
                .role(user.getRole())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .password(user.getPassword())          // ⚠️ NOT recommended
                .secretKey(user.getSecretKey())        // ⚠️ NOT recommended

                // Contact
                .contactNumber(user.getContactNumber())
                .isContactVerified(user.isContactVerified())
                .isEmailVerified(user.isEmailVerified())

                // Account
                .accountBalance(user.getAccountBalance())
                .status(user.isStatus())
                .isVerified(user.isVerified())

                // Merchant Settings
                .merchantHostedFlag(user.isMerchantHostedFlag())
                .processingMode(user.getProcessingMode())
                .appKey(user.getAppKey())

                // Permissions
                .isPayinEnabled(user.isPayinEnabled())
                .isPayoutEnabled(user.isPayoutEnabled())
                .isPayinGstEnabled(user.isPayinGstEnabled())
                .isPayoutGstEnabled(user.isPayoutGstEnabled())
                .isAccountNonExpired(user.isAccountNonExpired())
                .isAccountNonLocked(user.isAccountNonLocked())
                .isCredentialsNonExpired(user.isCredentialsNonExpired())
                .isCrypto(user.isCrypto())

                // Webhooks
                .payinWebhookUrl(user.getPayinWebhookUrl())
                .payoutWebhookUrl(user.getPayoutWebhookUrl())

                // Flatten DBRefs
                .currencyCode(user.getCurrencies() == null
                        ? Collections.emptySet()
                        : user.getCurrencies()
                        .stream()
                        .map(c -> c.getCurrencyCode())
                        .collect(Collectors.toSet()))
                .countryCode(user.getCountries() == null
                        ? Collections.emptySet()
                        : user.getCountries()
                        .stream()
                        .map(c -> c.getCountryCode())
                        .collect(Collectors.toSet()))
                .bankAccounts(user.getBankDetails() == null
                        ? Collections.emptySet()
                        : user.getBankDetails()
                        .stream()
                        .map(b -> b.getBankAccountNumber())
                        .collect(Collectors.toSet()))

                // Audit
                .verificationDate(user.getVerificationDate())
                .createdDate(user.getCreatedDate())
                .lastModifiedDate(user.getLastModifiedDate())
                .createdBy(user.getCreatedBy())
                .lastModifiedBy(user.getLastModifiedBy())

                .build();
    }
    public static UserDetails buildSpringUser(UserCacheDTO user) {

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUserId())
                .password(user.getPassword())
                .authorities(user.getRole())
                .accountExpired(!user.isAccountNonExpired())
                .accountLocked(!user.isAccountNonLocked())
                .credentialsExpired(!user.isCredentialsNonExpired())
                .disabled(!user.isStatus())
                .build();
    }
    }





