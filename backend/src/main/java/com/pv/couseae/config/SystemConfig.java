package com.pv.couseae.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class SystemConfig {
    @Value("${RblPass}")
    private String rblPass;

    @Value("${RblAcctId}")
    private String rblAcctId;
    @Value("${spring.profiles.active:default}")
    private String activeProfile;
    // ✅ Skip wallet deduction for bhanve-prod profile
    public boolean isSkipWalletDeduction() {
        return "bhanve-prod".equalsIgnoreCase(activeProfile);
    }
    @Value("${CryptoBaseUrl}")
    private String cryptoBaseUrl;


    @Value("${juspay.base-url}") private String baseUrl;
    @Value("${juspay.merchant-id}") private String merchantId;
    @Value("${juspay.merchant-vpa}") private String merchantVpa;
    @Value("${juspay.channel-id}") private String channelId;
    // @Value("${juspay.kid}") private String kid;
    @Value("${juspay.jws-kid}")         private String jwsKid;   // ✅ was: juspay.kid
    @Value("${juspay.jwe-kid}")         private String jweKid;   // ✅ new
    @Value("${juspay.private-key-path}") private String privateKeyPath;
    @Value("${juspay.public-key-path}") private String publicKeyPath;

}
