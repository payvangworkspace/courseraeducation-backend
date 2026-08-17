package com.pv.couseae.utill;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class RandomStringGenerator {
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String AESKeyGeneratorBase64()  {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256); // AES-256
            SecretKey secretKey = keyGen.generateKey();
            String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
            System.out.println("Generated AES-256 Key: " + encodedKey);
            return encodedKey;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String generateRandomString(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = RANDOM.nextInt(ALPHANUMERIC.length());
            builder.append(ALPHANUMERIC.charAt(randomIndex));
        }
        return builder.toString();
    }

    public static String generateSecretKey() {
        StringBuilder builder = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            int randomIndex = RANDOM.nextInt(ALPHANUMERIC.length());
            builder.append(ALPHANUMERIC.charAt(randomIndex));
        }
        return builder.toString();
    }

}

