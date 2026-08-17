package com.pv.couseae.utill;

import com.pv.couseae.config.SystemConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RBLSSLClientUtill {
    private final SystemConfig systemConfig;

    public CloseableHttpClient rblHttpClientOld()  { //this is working fine method
        String pass="uat@12345";
        String keystorePath = "rbl-client-keystore.p12";
        String truststorePath = "rbl-zth-truststore.jks";
        String activeProfiles =systemConfig.getActiveProfile();
        log.info("The active profiles are: {}",activeProfiles);
        boolean isProduction = "prod".equalsIgnoreCase(activeProfiles);
        if(isProduction){
            pass="live@12345";
            keystorePath="live-rbl-client-keystore.p12";
            truststorePath = "live-rbl-truststore.jks";
        }

        log.info("Creating SSL client for keystore: {}, truststore: {}", keystorePath, truststorePath);
        // ========= CLIENT KEYSTORE (mTLS) =========
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            ClassPathResource p12Resource = new ClassPathResource(keystorePath);
            try (InputStream is = p12Resource.getInputStream()) {
                keyStore.load(is, pass.toCharArray());
            }

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, pass.toCharArray());

            // ========= TRUSTSTORE (RBL certs) =========
            KeyStore trustStore = KeyStore.getInstance("PKCS12");
            ClassPathResource resource = new ClassPathResource(truststorePath);

            log.info("Truststore exists: {}", resource.exists());

            try (InputStream is = resource.getInputStream()) {
                trustStore.load(is, pass.toCharArray());
            }

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            // ========= SSL CONTEXT (TLS 1.2 + mTLS) =========
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(
                    kmf.getKeyManagers(),      // ✅ CLIENT CERT
                    tmf.getTrustManagers(),    // ✅ SERVER TRUST
                    null
            );

            SSLConnectionSocketFactory csf =
                    new SSLConnectionSocketFactory(
                            sslContext,
                            new String[]{"TLSv1.2"},
                            null,
                            SSLConnectionSocketFactory.getDefaultHostnameVerifier()
                    );

            return  HttpClients.custom()
                    .setSSLSocketFactory(csf)
                    .build();
        } catch (Exception e) {
            log.error("Error creating SSL client", e);
        }
       return null;
    }






}