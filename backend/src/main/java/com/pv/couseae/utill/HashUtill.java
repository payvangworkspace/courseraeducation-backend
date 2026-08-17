package com.pv.couseae.utill;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Component
public class HashUtill {

    public static String encryptHmac(String message, String secretKey) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secretKeySpec);

        byte[] raw = sha256_HMAC.doFinal(message.getBytes(StandardCharsets.UTF_8));

        StringBuilder hex = new StringBuilder();
        for (byte b : raw) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
    public static String decodeQRCodeFromUrl(String imageUrl) throws Exception {
        // Download image
        URL url = new URL(imageUrl);
        InputStream inputStream = url.openStream();
        BufferedImage bufferedImage = ImageIO.read(inputStream);

        if (bufferedImage == null) {
            throw new Exception("Invalid image or unsupported format!");
        }

        // Decode QR
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result result = new MultiFormatReader().decode(bitmap);

        return result.getText(); // UPI Intent or Razorpay URL
    }
    public String ShadvalResponceDecrypt(String jwt, String secretKey) throws Exception {

        // Split the token
        String[] parts = jwt.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT format");
        }

        String payload = parts[1];

        // Base64 decode
        String decoded = new String(Base64.getDecoder().decode(payload), StandardCharsets.UTF_8);

        // Step 1: Remove surrounding quotes
        if (decoded.startsWith("\"") && decoded.endsWith("\"")) {
            decoded = decoded.substring(1, decoded.length() - 1);
        }

        // Step 2: Unescape JSON inside
        decoded = decoded.replace("\\\"", "\"");

        return decoded;
    }
}
