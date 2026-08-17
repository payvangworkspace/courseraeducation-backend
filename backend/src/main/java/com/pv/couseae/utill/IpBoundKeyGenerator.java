package com.pv.couseae.utill;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Component
public class IpBoundKeyGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String HMAC_ALGO = "HmacSHA256";
    private final String hmacSecret;

    // Spring will inject the value of `api.hmac.secret` here
    public IpBoundKeyGenerator(@Value("${api.hmac.secretkey}") String hmacSecret) {
        if (hmacSecret == null || hmacSecret.isBlank()) {
            throw new IllegalArgumentException("HMAC secret cannot be empty!");
        }
        this.hmacSecret = hmacSecret;
    }

    public Pair<String,String> generateTokenForIp(String ip) {
        byte[] nonceBytes = new byte[12]; // 12 bytes random nonce
        RANDOM.nextBytes(nonceBytes);
        String nonce = bytesToHex(nonceBytes);
        String ts = String.valueOf(Instant.now().getEpochSecond());

        String data = ip + ":" + nonce + ":" + ts;
        String token = hmacBase64(data);
        return new Pair<>(nonce, token);
    }

    private String hmacBase64(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            byte[] secretBytes = hmacSecret.getBytes(StandardCharsets.UTF_8);
            mac.init(new SecretKeySpec(secretBytes, HMAC_ALGO));
            byte[] out = mac.doFinal(data.getBytes("UTF-8"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String bytesToHex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length*2);
        for (byte x : b) { sb.append(String.format("%02x", x)); }
        return sb.toString();
    }

    // simple pair class (or use org.apache.commons.lang3.tuple.Pair)
    public static class Pair<A,B> { public final A a; public final B b; public Pair(A a,B b){this.a=a;this.b=b;} }
}