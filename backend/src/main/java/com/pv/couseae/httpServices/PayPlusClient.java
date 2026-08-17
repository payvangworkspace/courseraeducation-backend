package com.pv.couseae.httpServices;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pv.couseae.Dtos.payplus.*;
import com.pv.couseae.entities.ApiMaster;
import com.pv.couseae.model.PayinRequestModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayPlusClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;
    private static final String HMAC_ALGO = "HmacSHA256";

    // ═══════════════════════════════════════════════════════════
    // Public API
    // ═══════════════════════════════════════════════════════════

    /**
     * Creates a payin (deposit) transaction with PayPlus.
     *
     * @param apiMaster holds baseUrl, endpoint, and the merchant's x-api-key (stored as secretKey)
     * @param req       payin request containing amount, orderId, customer name, and udf fields
     */
    public PayPlusPayinResponseDto createPayin(ApiMaster apiMaster, PayinRequestModel req) throws Exception {

        String merchantOrderId = req.getOrderId();
        double amount = req.getPayableAmount().doubleValue();
        String username = req.getFirstname() + " " + req.getLastname();

        // NOTE: PayinRequestModel has no explicit "playerId" field — using udf1 as the
        // player identifier carrier for now. Swap this to the correct source field
        // once you confirm where playerId actually comes from upstream.
        String playerId = req.getUdf1();

        PayPlusPayinRequestDto requestDto = PayPlusPayinRequestDto.builder()
                .amount(amount)
                .merchantOrderId(merchantOrderId)
                .username(username)
                .customerMeta(PayPlusPayinRequestDto.CustomerMeta.builder()
                        .playerId(playerId)
                        .build())
                .build();

        HttpHeaders headers = buildHeaders(apiMaster.getSecretKey());

        String url = apiMaster.getBaseUrl() + apiMaster.getEndpoint();
        log.info("Calling PayPlus Payin Create: {}", url);
        log.info("PayPlus Payin request body: merchantOrderId={}, amount={}, username={}",
                merchantOrderId, amount, username);

        HttpEntity<PayPlusPayinRequestDto> entity = new HttpEntity<>(requestDto, headers);

        ResponseEntity<String> rawResponse;
        try {
            rawResponse = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        } catch (RestClientException ex) {
            log.error("PayPlus Payin call failed for merchantOrderId={}: {}", merchantOrderId, ex.getMessage(), ex);
            throw ex;
        }

        log.info("PayPlus Payin status: {}", rawResponse.getStatusCode());

        if (rawResponse.getStatusCode().is2xxSuccessful()) {
            log.info("PayPlus Payin raw response: {}", rawResponse.getBody());
            return parseResponse(rawResponse.getBody());
        } else {
            log.warn("PayPlus Payin unsuccessful response: {}", rawResponse.getBody());
            return null;
        }
    }
    /**
     * Submits the UTR (Unique Transaction Reference) for a previously created payin order,
     * confirming the bank/UPI transfer reference against PayPlus's records.
     *
     * @param apiMaster holds baseUrl and the merchant's x-api-key (stored as secretKey)
     * @param orderId   the PayPlus orderId returned from createPayin (not merchantOrderId)
     * @param utr       the UTR/reference number from the customer's bank transfer
     */
    public PayPlusSubmitUtrResponseDto submitUtr(ApiMaster apiMaster, String orderId, String utr) throws Exception {

        PayPlusSubmitUtrRequestDto requestDto = PayPlusSubmitUtrRequestDto.builder()
                .orderId(orderId)
                .utr(utr)
                .build();

        HttpHeaders headers = buildHeaders(apiMaster.getSecretKey());

        String url = apiMaster.getBaseUrl() + apiMaster.getEndpoint();
        log.info("Calling PayPlus Submit UTR: {}", url);
        log.info("PayPlus Submit UTR request body: orderId={}, utr={}", orderId, utr);

        HttpEntity<PayPlusSubmitUtrRequestDto> entity = new HttpEntity<>(requestDto, headers);

        ResponseEntity<String> rawResponse;
        try {
            rawResponse = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        } catch (RestClientException ex) {
            log.error("PayPlus Submit UTR call failed for orderId={}: {}", orderId, ex.getMessage(), ex);
            throw ex;
        }

        log.info("PayPlus Submit UTR status: {}", rawResponse.getStatusCode());

        if (rawResponse.getStatusCode().is2xxSuccessful()) {
            log.info("PayPlus Submit UTR raw response: {}", rawResponse.getBody());
            return parseSubmitUtrResponse(rawResponse.getBody());
        } else {
            log.warn("PayPlus Submit UTR unsuccessful response: {}", rawResponse.getBody());
            return null;
        }
    }

    private PayPlusSubmitUtrResponseDto parseSubmitUtrResponse(String rawBody) throws Exception {
        if (rawBody == null) return null;

        JsonNode root = objectMapper.readTree(rawBody);
        log.info("PayPlus Submit UTR full response: {}", root);

        return objectMapper.treeToValue(root, PayPlusSubmitUtrResponseDto.class);
    }
    /**
     * Checks the current status of a payin order with PayPlus.
     *
     * @param apiMaster       holds baseUrl and the merchant's x-api-key (stored as secretKey)
     * @param merchantOrderId your original order/deposit reference passed at creation time
     */
    public PayPlusStatusResponseDto checkStatus(ApiMaster apiMaster, String merchantOrderId) throws Exception {

        PayPlusStatusRequestDto requestDto = PayPlusStatusRequestDto.builder()
                .merchantOrderId(merchantOrderId)
                .build();

        HttpHeaders headers = buildHeaders(apiMaster.getSecretKey());

        String url = apiMaster.getBaseUrl() + "/api/v1/payin/status";
        log.info("Calling PayPlus Check Status: {}", url);
        log.info("PayPlus Check Status request body: merchantOrderId={}", merchantOrderId);

        HttpEntity<PayPlusStatusRequestDto> entity = new HttpEntity<>(requestDto, headers);

        ResponseEntity<String> rawResponse;
        try {
            rawResponse = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        } catch (RestClientException ex) {
            log.error("PayPlus Check Status call failed for merchantOrderId={}: {}", merchantOrderId, ex.getMessage(), ex);
            throw ex;
        }

        log.info("PayPlus Check Status status: {}", rawResponse.getStatusCode());

        if (rawResponse.getStatusCode().is2xxSuccessful()) {
            log.info("PayPlus Check Status raw response: {}", rawResponse.getBody());
            return parseStatusResponse(rawResponse.getBody());
        } else {
            log.warn("PayPlus Check Status unsuccessful response: {}", rawResponse.getBody());
            return null;
        }
    }

    private PayPlusStatusResponseDto parseStatusResponse(String rawBody) throws Exception {
        if (rawBody == null) return null;

        JsonNode root = objectMapper.readTree(rawBody);
        log.info("PayPlus Check Status full response: {}", root);

        return objectMapper.treeToValue(root, PayPlusStatusResponseDto.class);
    }
    // ═══════════════════════════════════════════════════════════
    // Private Helpers
    // ═══════════════════════════════════════════════════════════

    private PayPlusPayinResponseDto parseResponse(String rawBody) throws Exception {
        if (rawBody == null) return null;

        JsonNode root = objectMapper.readTree(rawBody);
        log.info("PayPlus Payin full response: {}", root);

        return objectMapper.treeToValue(root, PayPlusPayinResponseDto.class);
    }

    private HttpHeaders buildHeaders(String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        return headers;
    }


    /**
     * Verifies the x-payplus-signature header against the raw webhook body,
     * using HMAC-SHA256 with a constant-time comparison to prevent timing attacks.
     *
     * @param rawBody        the exact raw request body bytes received (not re-serialized)
     * @param signatureHeader the value of the x-payplus-signature header
     * @param webhookSecret  your PayPlus webhook signing secret
     * @return true if the signature is valid, false otherwise
     */
    public boolean verifyWebhookSignature(String rawBody, String signatureHeader, String webhookSecret) {

        if (signatureHeader == null || signatureHeader.isBlank()) {
            log.warn("PayPlus webhook rejected — missing x-payplus-signature header");
            return false;
        }

        if (rawBody == null) {
            log.warn("PayPlus webhook rejected — empty request body");
            return false;
        }

        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO));
            byte[] rawHmac = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : rawHmac) {
                hex.append(String.format("%02x", b));
            }
            String expectedSignature = hex.toString();

            boolean valid = constantTimeEquals(expectedSignature, signatureHeader);
            if (!valid) {
                log.warn("PayPlus webhook rejected — signature mismatch");
            }
            return valid;

        } catch (Exception ex) {
            log.error("PayPlus webhook signature verification failed: {}", ex.getMessage(), ex);
            return false;
        }
    }

    /**
     * Constant-time string comparison to prevent timing attacks —
     * always compares the full length instead of short-circuiting on first mismatch.
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}