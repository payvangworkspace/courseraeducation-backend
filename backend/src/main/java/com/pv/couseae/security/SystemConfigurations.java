package com.pv.couseae.security;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class SystemConfigurations {
    @Getter
    @Value("${token}")
    private static String token;

    @Getter
    @Value("${checkoutUrl}")
    private static String checkoutUrl;

    @Getter
    @Value("${serviceUrl}")
    private static String serviceUrl;

    @Getter
    @Value("${email}")
    private static String senderMail;



    // Inject value into this method
    @Value("${checkoutUrl}")
    public void setToken(String token) {
        SystemConfigurations.token = token;
    }

    // Inject value into this method
    @Value("${checkoutUrl}")
    public void setCheckoutUrl(String checkoutUrl) {
        SystemConfigurations.checkoutUrl = checkoutUrl;
    }

    // Inject value into this method
    @Value("${serviceUrl}")
    public void setServiceUrl(String serviceUrl) {
        SystemConfigurations.serviceUrl = serviceUrl;
    }

    @Value("${email}")
    public void setSenderMail(String senderMail) {
        SystemConfigurations.senderMail = senderMail;
    }

    @Value("${CryptoBaseUrl}")
    private String cryptoBaseUrl;
    @Value("${CryptoAdminApiKey}")
    private String cryptoAdminApiKey;
//    @Value("${CryptoBaseUrl}")
//    public void setCryptoBaseUrl(String cryptoBaseUrl) {
//        SystemConfigurations.cryptoBaseUrl = cryptoBaseUrl;
//    }
}
