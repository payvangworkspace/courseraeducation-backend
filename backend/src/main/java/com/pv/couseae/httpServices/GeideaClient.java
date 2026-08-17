package com.pv.couseae.httpServices;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pv.couseae.Dtos.geidea.*;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Geidea HPP Checkout client — UAE platform (https://api.geidea.ae).
 *
 * ApiMaster mapping for this gateway:
 *   baseUrl    -> https://api.geidea.ae
 *   endpoint   -> /payment-intent/api/v2/direct/session
 *   apiKey     -> merchant PUBLIC KEY   (basic-auth username)
 *   secretKey  -> merchant API PASSWORD (basic-auth password AND the HMAC signing key)
 *
 * If your ApiMaster has no separate apiKey column, add one — Geidea needs both values and the
 * public key is also a signature input, so it cannot be derived from the secret.
 *
 * Geidea runs KSA, Egypt and UAE as separate platforms with separate credentials. Keep baseUrl
 * pointed at api.geidea.ae for this integration.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeideaClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    private static final String HMAC_ALGO = "HmacSHA256";

    /**
     * Geidea's own sample formats the timestamp as date_format($date, "Y/m/d H:i:s") —
     * slashes, not dashes, and no timezone marker. The exact string signed must be the exact
     * string sent in the body.
     */
    private static final DateTimeFormatter TS_FORMAT =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    /**
     * Zone used to render that timestamp. Geidea does not document which zone their validator
     * expects; UTC is the safe default. If sessions start failing signature validation after a
     * server move, check this first.
     */
    private static final ZoneId TS_ZONE = ZoneId.of("UTC");

    private static final String CHECKOUT_SCRIPT_URL =
            "https://payments.geidea.ae/hpp/geideaCheckout.min.js";

    /**
     * Redirect-mode hosted page. INFERRED from the UAE script host — Geidea documents this URL
     * against the KSA host only. Popup and drop-in modes do not use it; confirm with Geidea
     * before relying on redirection mode.
     */
    private static final String HOSTED_CHECKOUT_URL =
            "https://payments.geidea.ae/hpp/checkout/?";

    // ═══════════════════════════════════════════════════════════
    // Public API
    // ═══════════════════════════════════════════════════════════

    /**
     * Creates a checkout session with Geidea. The returned session id is the only value that
     * should be handed to the browser — never the API password.
     *
     * @param apiMaster holds baseUrl, endpoint, apiKey (public key) and secretKey (API password)
     * @param req       payin request containing amount, orderId, customer name and udf fields
     */
    public GeideaSessionResponseDto createSession(ApiMaster apiMaster, PayinRequestModel req) throws Exception {

        String merchantOrderId = req.getOrderId();

        // Geidea signs the amount at exactly 2 decimals. Normalise ONCE and use the same value
        // for both the signature and the body — a raw toString() can carry a different scale
        // (BigDecimal is stored as a string in Mongo) and will not match.
        BigDecimal amount = req.getPayableAmount().setScale(2, RoundingMode.HALF_UP);

        // AED is the UAE default. Anything else needs multicurrency enabled on the account.
        // There is no INR on this gateway.
        String currency = req.getCurrencyCode() != null ? req.getCurrencyCode() : "AED";

        String timestamp = LocalDateTime.now(TS_ZONE).format(TS_FORMAT);

        String signature = generateSessionSignature(
                apiMaster.getClientId(), amount, currency, merchantOrderId,
                timestamp, apiMaster.getSecretKey());

        GeideaSessionRequestDto requestDto = GeideaSessionRequestDto.builder()
                .amount(amount)
                .currency(currency)
                .timestamp(timestamp)
                .signature(signature)
                .merchantReferenceId(merchantOrderId)
                .callbackUrl(apiMaster.getWebhoockUrl())
                .returnUrl(apiMaster.getResponseUrl())
                .paymentOperation("Pay")
                .language("en")
                .customer(GeideaSessionRequestDto.Customer.builder()
                        .firstName(req.getFirstname())
                        .lastName(req.getLastname())
                        .email(req.getEmailId())
                        .phoneNumber(req.getMobileNo())
                        .build())
                .build();

        HttpHeaders headers = buildHeaders(apiMaster.getClientId(), apiMaster.getSecretKey());

        String url = apiMaster.getBaseUrl() + apiMaster.getEndpoint();
        log.info("Calling Geidea Create Session: {}", url);
        log.info("Geidea Create Session request body: merchantReferenceId={}, amount={}, currency={}",
                merchantOrderId, amount, currency);

        HttpEntity<GeideaSessionRequestDto> entity = new HttpEntity<>(requestDto, headers);

        ResponseEntity<String> rawResponse;
        try {
            rawResponse = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        } catch (RestClientException ex) {
            log.error("Geidea Create Session call failed for merchantReferenceId={}: {}",
                    merchantOrderId, ex.getMessage(), ex);
            throw ex;
        }

        log.info("Geidea Create Session status: {}", rawResponse.getStatusCode());

        if (rawResponse.getStatusCode().is2xxSuccessful()) {
            log.info("Geidea Create Session raw response: {}", rawResponse.getBody());
            return parseSessionResponse(rawResponse.getBody());
        } else {
            log.warn("Geidea Create Session unsuccessful response: {}", rawResponse.getBody());
            return null;
        }
    }

    /**
     * Fetches the current status of an order using your own merchant reference.
     *
     * Unlike PayPlus this is a GET with the reference in the path, so there is no request body.
     *
     * @param apiMaster       holds baseUrl, apiKey (public key) and secretKey (API password)
     * @param merchantOrderId the merchantReferenceId sent at session creation
     */
    public GeideaOrderResponseDto checkStatus(ApiMaster apiMaster, String merchantOrderId) throws Exception {

        HttpHeaders headers = buildHeaders(apiMaster.getClientId(), apiMaster.getSecretKey());

        String url = apiMaster.getBaseUrl()
                + "/pgw/api/v1/direct/order/merchantReferenceId/" + merchantOrderId;

        log.info("Calling Geidea Check Status: {}", url);
        log.info("Geidea Check Status merchantReferenceId={}", merchantOrderId);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> rawResponse;
        try {
            rawResponse = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        } catch (RestClientException ex) {
            log.error("Geidea Check Status call failed for merchantReferenceId={}: {}",
                    merchantOrderId, ex.getMessage(), ex);
            throw ex;
        }

        log.info("Geidea Check Status status: {}", rawResponse.getStatusCode());

        if (rawResponse.getStatusCode().is2xxSuccessful()) {
            log.info("Geidea Check Status raw response: {}", rawResponse.getBody());
            return parseOrderResponse(rawResponse.getBody());
        } else {
            log.warn("Geidea Check Status unsuccessful response: {}", rawResponse.getBody());
            return null;
        }
    }

    /**
     * Refunds a captured order, fully or partially.
     *
     * @param apiMaster holds baseUrl, apiKey (public key) and secretKey (API password)
     * @param orderId   the Geidea orderId (not your merchantReferenceId)
     * @param amount    refund amount; pass the full order amount for a full refund
     * @param currency  must match the original order currency
     */
    public GeideaOrderResponseDto refund(ApiMaster apiMaster, String orderId,
                                         BigDecimal amount, String currency) throws Exception {

        BigDecimal refundAmount = amount.setScale(2, RoundingMode.HALF_UP);

        GeideaRefundRequestDto requestDto = GeideaRefundRequestDto.builder()
                .orderId(orderId)
                .amount(refundAmount)
                .currency(currency)
                .build();

        HttpHeaders headers = buildHeaders(apiMaster.getClientId(), apiMaster.getSecretKey());

        String url = apiMaster.getBaseUrl() + "/pgw/api/v1/direct/refund";
        log.info("Calling Geidea Refund: {}", url);
        log.info("Geidea Refund request body: orderId={}, amount={}, currency={}",
                orderId, refundAmount, currency);

        HttpEntity<GeideaRefundRequestDto> entity = new HttpEntity<>(requestDto, headers);

        ResponseEntity<String> rawResponse;
        try {
            rawResponse = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        } catch (RestClientException ex) {
            log.error("Geidea Refund call failed for orderId={}: {}", orderId, ex.getMessage(), ex);
            throw ex;
        }

        log.info("Geidea Refund status: {}", rawResponse.getStatusCode());

        if (rawResponse.getStatusCode().is2xxSuccessful()) {
            log.info("Geidea Refund raw response: {}", rawResponse.getBody());
            return parseOrderResponse(rawResponse.getBody());
        } else {
            log.warn("Geidea Refund unsuccessful response: {}", rawResponse.getBody());
            return null;
        }
    }

    /** UAE checkout script src, for whatever renders your checkout page. */
    public String checkoutScriptUrl() {
        return CHECKOUT_SCRIPT_URL;
    }

    /** Redirect-mode URL. See the constant's note — the UAE host here is inferred. */
    public String hostedCheckoutUrl(String sessionId) {
        return HOSTED_CHECKOUT_URL + sessionId;
    }

    // ═══════════════════════════════════════════════════════════
    // Private Helpers
    // ═══════════════════════════════════════════════════════════

    private GeideaSessionResponseDto parseSessionResponse(String rawBody) throws Exception {
        if (rawBody == null) return null;

        JsonNode root = objectMapper.readTree(rawBody);
        log.info("Geidea Create Session full response: {}", root);

        return objectMapper.treeToValue(root, GeideaSessionResponseDto.class);
    }

    private GeideaOrderResponseDto parseOrderResponse(String rawBody) throws Exception {
        if (rawBody == null) return null;

        JsonNode root = objectMapper.readTree(rawBody);
        log.info("Geidea order full response: {}", root);

        return objectMapper.treeToValue(root, GeideaOrderResponseDto.class);
    }

    /**
     * Geidea authenticates with HTTP Basic: public key as username, API password as password.
     * There is no x-api-key header on this gateway.
     */
    private HttpHeaders buildHeaders(String publicKey, String apiPassword) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String creds = publicKey + ":" + apiPassword;
        String encoded = Base64.getEncoder()
                .encodeToString(creds.getBytes(StandardCharsets.UTF_8));
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encoded);

        return headers;
    }

    // ═══════════════════════════════════════════════════════════
    // Signatures
    //
    // The Geidea docs are ambiguous: the prose says to "Hash (SHA-256) the concatenated string
    // by (MerchantAPIPassword)", but their PHP and C# samples both use HMAC-SHA256 keyed with
    // the API password. HMAC is implemented here because that is what the working samples do.
    //
    // The callback page repeats the same wording with no sample code, so verifyCallbackSignature
    // assumes HMAC as well. CONFIRM against a real sandbox callback before go-live — if it
    // mismatches, log the payload string built below and ask Geidea to confirm the field order,
    // rather than trying variants until one passes.
    //
    // Two different field sets are involved:
    //   session  : publicKey + amount + currency + merchantReferenceId + timestamp
    //   callback : publicKey + amount + currency + orderId + status + merchantReferenceId + timestamp
    // ═══════════════════════════════════════════════════════════

    /**
     * Signature for the Create Session request. Base64, not hex — unlike the PayPlus webhook
     * signature, which is hex.
     */
    public String generateSessionSignature(String publicKey, BigDecimal amount, String currency,
                                           String merchantReferenceId, String timestamp,
                                           String apiPassword) {

        String payload = publicKey
                + amount.setScale(2, RoundingMode.HALF_UP).toPlainString()
                + currency
                + nullToEmpty(merchantReferenceId)
                + timestamp;

        return hmacBase64(payload, apiPassword);
    }

    /** Signature as Geidea computes it for the callback payload. */
    public String generateCallbackSignature(String publicKey, BigDecimal amount, String currency,
                                            String orderId, String status,
                                            String merchantReferenceId, String timestamp,
                                            String apiPassword) {

        String payload = publicKey
                + amount.setScale(2, RoundingMode.HALF_UP).toPlainString()
                + currency
                + nullToEmpty(orderId)
                + nullToEmpty(status)
                + nullToEmpty(merchantReferenceId)
                + timestamp;

        return hmacBase64(payload, apiPassword);
    }

    /**
     * Verifies the signature carried inside the Geidea callback body.
     *
     * Note the difference from PayPlus: Geidea does not send a signature header over the raw
     * body. The signature is a field in the JSON payload and is computed over a fixed set of
     * payload fields, so the callback must be parsed before it can be verified. That also means
     * re-serialisation is not a concern here.
     *
     * @param callback      the parsed callback payload
     * @param publicKey     merchant public key (ApiMaster.apiKey)
     * @param apiPassword   merchant API password (ApiMaster.secretKey)
     */
    public boolean verifyCallbackSignature(GeideaCallbackDto callback,
                                           String publicKey, String apiPassword) {

        if (callback == null) {
            log.warn("Geidea callback rejected — empty payload");
            return false;
        }

        if (callback.getSignature() == null || callback.getSignature().isBlank()) {
            log.warn("Geidea callback rejected — missing signature field");
            return false;
        }

        if (callback.getAmount() == null) {
            log.warn("Geidea callback rejected — missing amount, cannot verify signature");
            return false;
        }

        try {
            String expectedSignature = generateCallbackSignature(
                    publicKey,
                    callback.getAmount(),
                    callback.getCurrency(),
                    callback.getOrderId(),
                    callback.getStatus(),
                    callback.getMerchantReferenceId(),
                    callback.getTimestamp(),
                    apiPassword);

            boolean valid = constantTimeEquals(expectedSignature, callback.getSignature());
            if (!valid) {
                log.warn("Geidea callback rejected — signature mismatch for orderId={} ref={}",
                        callback.getOrderId(), callback.getMerchantReferenceId());
            }
            return valid;

        } catch (Exception ex) {
            log.error("Geidea callback signature verification failed: {}", ex.getMessage(), ex);
            return false;
        }
    }

    private String hmacBase64(String payload, String apiPassword) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(apiPassword.getBytes(StandardCharsets.UTF_8), HMAC_ALGO));
            byte[] rawHmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception ex) {
            // Never log the payload — it is keyed with the API password.
            throw new IllegalStateException("Failed to compute Geidea signature", ex);
        }
    }

    /**
     * Constant-time comparison to prevent timing attacks. MessageDigest.isEqual is constant-time
     * in modern JDKs and avoids the manual loop.
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}