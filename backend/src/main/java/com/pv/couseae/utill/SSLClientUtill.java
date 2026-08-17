package com.pv.couseae.utill;

import com.pv.couseae.config.SystemConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SSLClientUtill {
    private final SystemConfig systemConfig;

    @PostConstruct
    public void init() {
        log.info("SSLClientUtill initialized");
    }

    // ----------------- SINGLETON CONNECTION POOL -----------------
    @Bean
    public PoolingHttpClientConnectionManager rblConnectionManager() throws Exception {
        boolean isProd = "prod".equalsIgnoreCase(systemConfig.getActiveProfile());

        String keystorePath = isProd ? "live-rbl-client-keystore.p12" : "rbl-client-keystore.p12";
        String truststorePath = isProd ? "live-rbl-truststore.jks" : "rbl-zth-truststore.jks";
        String password = isProd ? "live@12345" : "uat@12345";

        // SSL Context (TLS 1.2 + mTLS)
        SSLContext sslContext = SSLContextBuilder.create()
                .setProtocol("TLSv1.2")
                .loadKeyMaterial(new ClassPathResource(keystorePath).getURL(),password.toCharArray(),password.toCharArray())
                .loadTrustMaterial(new ClassPathResource(truststorePath).getURL(),password.toCharArray())
                .build();

        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext,
                new String[]{"TLSv1.2"},
                null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());

        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslSocketFactory)
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .build();

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
        cm.setMaxTotal(50);
        cm.setDefaultMaxPerRoute(20);
        cm.setValidateAfterInactivity(5000);

        // Scheduled idle/expired connection cleanup
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "HttpClient-CM-Cleanup");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            try {
                cm.closeExpiredConnections();
                cm.closeIdleConnections(30, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.warn("Error during connection pool cleanup", e);
            }
        }, 30, 30, TimeUnit.SECONDS);

        log.info("RBL Connection Manager initialized");
        return cm;
    }

    // ----------------- HTTP CLIENT BEAN -----------------
    @Bean
    @Primary
    public CloseableHttpClient rblHttpClient(PoolingHttpClientConnectionManager cm) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(10_000)
                .setConnectionRequestTimeout(5_000)
                .setSocketTimeout(20_000)
                .build();

        HttpRequestRetryHandler retryHandler = (exception, executionCount, context) -> {
            if (executionCount > 2) return false; // max 2 retries
            if (exception instanceof SSLException) return false; // never retry SSL

            HttpRequest request = (HttpRequest) context.getAttribute(HttpCoreContext.HTTP_REQUEST);
            if (request instanceof HttpEntityEnclosingRequest) return false; // no retry for POST/PUT

            return exception instanceof org.apache.http.NoHttpResponseException
                    || exception instanceof java.net.SocketTimeoutException
                    || exception instanceof org.apache.http.conn.ConnectTimeoutException;
        };

        CloseableHttpClient client = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig)
                .setRetryHandler(retryHandler)
                .disableConnectionState() // safe for multi-threaded use
                .build();

        log.info("RBL HttpClient initialized");
        return client;
    }

}