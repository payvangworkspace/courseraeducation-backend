package com.pv.couseae.httpServices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pv.couseae.Dtos.Crypto.CryptoOrderRequest;
import com.pv.couseae.Dtos.HdfcSessionRequestDTO;
import com.pv.couseae.config.SystemConfig;
import com.pv.couseae.entities.ApiMaster;
import com.pv.couseae.model.PayinRequestModel;
import com.pv.couseae.utill.HashUtill;
import com.pv.couseae.utill.TransactionIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class HttpService {

    // ─── Dependencies ─────────────────────────────────────────────────────────
    private final HashUtill hashUtill;
    private final RestTemplate restTemplate;
    private final TransactionIdGenerator transactionIdGenerator;
    private final SystemConfig systemConfig;
    private final ObjectMapper objectMapper;   // injected — not instantiated per call

    // ─── Constants ────────────────────────────────────────────────────────────
    private static final String CRYPTO_ORDERS_PATH   = "/api/v1/orders";
    private static final String PAYMENT_MODE_ALL     = "ALL";
    private static final String PAYMENT_MODE_QRCODE  = "QRCODE";


    // ─────────────────────────────────────────────────────────────────────────
    // CRYPTO
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a crypto order via the ZenithPay crypto gateway.
     */
    public String createCryptoOrder(CryptoOrderRequest req, String apiKey) {
        String url = systemConfig.getCryptoBaseUrl() + CRYPTO_ORDERS_PATH;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("x-api-key", apiKey);

        HttpEntity<CryptoOrderRequest> entity = new HttpEntity<>(req, headers);

        // ✅ Use injected restTemplate — not a new local instance
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        log.info("Crypto Order Status: {}", response.getStatusCode());
        log.info("Crypto Order Response: {}", response.getBody());
        return response.getBody();
    }


    // ─────────────────────────────────────────────────────────────────────────
    // HDFC
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates an HDFC payment session and returns the raw JSON response.
     */
    public String createHDFCPaymentSession(ApiMaster apiMaster, PayinRequestModel payinreqmodel) {
        log.info("📤 Initiating HDFC session for orderId: {}", payinreqmodel.getOrderId());

        HdfcSessionRequestDTO req = HdfcSessionRequestDTO.builder()
                .order_id(payinreqmodel.getOrderId())
                .amount(String.valueOf(payinreqmodel.getPayableAmount()))
                .customer_id(apiMaster.getMerchantId())
                .customer_email(payinreqmodel.getEmailId())
                .customer_phone(payinreqmodel.getMobileNo())
                .payment_page_client_id("hdfcmaster")
                .action("paymentPage")
                .currency(payinreqmodel.getCurrencyCode())
                .return_url(apiMaster.getWebhoockUrl())
                .description(payinreqmodel.getPaymentRemarks())
                .first_name(payinreqmodel.getFirstname())
                .last_name(payinreqmodel.getLastname())
                .build();

        String url = apiMaster.getBaseUrl() + "/session";

        try {
            HttpEntity<HdfcSessionRequestDTO> entity = new HttpEntity<>(req, buildHdfcHeaders(apiMaster));
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            log.info("✅ HDFC Session Response: {}", response.getBody());
            return response.getBody();
        } catch (Exception e) {
            log.error("❌ Error creating HDFC session for orderId={}", payinreqmodel.getOrderId(), e);
            return null;
        }
    }

    /**
     * Fetches the current order status from HDFC.
     */
    public String getHdfcOrderStatus(ApiMaster apiMaster, String orderId) {
        log.info("📤 Checking HDFC order status for orderId: {}", orderId);

        String finalUrl = (apiMaster.getBaseUrl() + apiMaster.getEndpoint())
                .replace("{order_id}", orderId.trim());

        // NOTE: HDFC status API requires form-urlencoded content type
        HttpHeaders headers = buildHdfcHeaders(apiMaster);
        headers.set("version", "2023-06-30");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        try {
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(finalUrl, HttpMethod.GET, entity, String.class);
            log.info("✅ HDFC Order Status Response: {}", response.getBody());
            return response.getBody();
        } catch (Exception e) {
            log.error("❌ Error checking HDFC order status for orderId={}", orderId, e);
            return null;
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // SHADVAL
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a standard Shadval payment session (all payment modes).
     */
    public String createPaymentSessionShadval(ApiMaster apiMaster, PayinRequestModel requestDTO) throws Exception {
        log.info("📤 Initiating Shadval session for orderId: {}", requestDTO.getOrderId());
        return createShadvalSession(apiMaster, requestDTO, PAYMENT_MODE_ALL);
    }

    /**
     * Creates a Shadval QR payment session.
     */
    public String createQRPaymentSessionShadval(ApiMaster apiMaster, PayinRequestModel requestDTO) throws Exception {
        log.info("📤 Initiating Shadval QR session for orderId: {}", requestDTO.getOrderId());
        return createShadvalSession(apiMaster, requestDTO, PAYMENT_MODE_QRCODE);
    }

    /**
     * Verifies a Shadval payment status by orderId and paymentId.
     */
    public String paymentStatusShadval(ApiMaster apiMaster, String orderId, String paymentId) throws Exception {
        log.info("📤 Shadval verify for orderId: {}", orderId);

        String url       = apiMaster.getBaseUrl() + apiMaster.getEndpoint();
        String uniqTxnId = transactionIdGenerator.generate15DigitTxnId();
        String hashText  = apiMaster.getSecretKey() + apiMaster.getMerchantId()
                + uniqTxnId + orderId + paymentId;

        log.info("Shadval Hashtext: {} | key: {}", hashText, apiMaster.getSecretKey());
        String encPayload = hashUtill.encryptHmac(hashText, apiMaster.getSecretKey());
        log.info("Shadval EncPayload: {}", encPayload);

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("merchant_id",       apiMaster.getMerchantId());
            requestBody.put("unique_request_id", uniqTxnId);
            requestBody.put("txn_unique_id",     orderId);
            requestBody.put("payment_id",        paymentId);

            HttpEntity<?> entity = new HttpEntity<>(requestBody,
                    buildShadvalHeaders(apiMaster.getSecretKey(), encPayload));
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            log.info("✅ Shadval Status Response: {}", response.getBody());
            return response.getBody();
        } catch (Exception e) {
            log.error("❌ Error checking Shadval payment status for orderId={}", orderId, e);
            return null;
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // GENERIC HTTP UTILITIES
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Simple GET request with no authentication headers.
     */
    public String getRequestWithoutHeader(String url) {
        try {
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            log.error("❌ Error in getRequestWithoutHeader for url={}", url, e);
            return null;
        }
    }

    /**
     * Async fire-and-forget POST with JSON content type and no auth.
     */
    @Async
    public void postRequestWithoutHeader(String url, Object requestBody) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Object> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            log.info("postRequestWithoutHeader response: {}", response.getBody());
        } catch (Exception e) {
            log.error("❌ Error in postRequestWithoutHeader for url={}", url, e.getMessage());
        }
    }

    /**
     * GET request with Bearer token authorization.
     */
    public String getRequestWithHeader(String url, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.set("Accept", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("❌ Error in getRequestWithHeader for url={}", url, e);
            return null;
        }
    }

    /**
     * POST request with Bearer token authorization.
     */
    public String postRequestWithHeaderAndBearerToken(String url, String token, Object requestBody) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("❌ Error in postRequestWithHeaderAndBearerToken for url={}", url, e);
            return null;
        }
    }

    /**
     * POST request with fully custom headers provided by the caller.
     */
    public String postRequestWithCustomHeader(String url, Object requestBody, HttpHeaders headers) {
        try {
            HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("❌ Error in postRequestWithCustomHeader for url={}", url, e);
            return null;
        }
    }

    /**
     * POST request with HTTP Basic authentication (username + password).
     */
    public String postRequestWithHeaderAndBasicToken(String url, String userName,
                                                     String password, Object requestBody) {
        try {
            String encodedCredentials = Base64.getEncoder()
                    .encodeToString((userName + ":" + password).getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + encodedCredentials);

            // ✅ Use injected objectMapper — not new ObjectMapper() per call
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            HttpEntity<Object> entity = new HttpEntity<>(jsonBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            log.info("postRequestWithHeaderAndBasicToken response: {}", response.getBody());
            return response.getBody();
        } catch (RestClientException | JsonProcessingException e) {
            log.error("❌ Error in postRequestWithHeaderAndBasicToken for url={}", url, e);
            return null;
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Core Shadval session creator shared by standard and QR session methods.
     * Differs only by the paymentMode value passed in.
     */
    private String createShadvalSession(ApiMaster apiMaster,
                                        PayinRequestModel requestDTO,
                                        String paymentMode) throws Exception {
        String url    = apiMaster.getBaseUrl() + apiMaster.getEndpoint();
        String amount = String.valueOf(requestDTO.getPayableAmount());

        String hashText = apiMaster.getSecretKey() + apiMaster.getMerchantId()
                + amount + requestDTO.getOrderId()
                + requestDTO.getFirstname() + requestDTO.getEmailId()
                + requestDTO.getMobileNo();

        log.info("Shadval Hashtext: {} | key: {}", hashText, apiMaster.getSecretKey());
        String encPayload = hashUtill.encryptHmac(hashText, apiMaster.getSecretKey());
        log.info("Shadval EncPayload: {}", encPayload);

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("merchant_id",   apiMaster.getMerchantId());
            requestBody.put("txn_amount",    amount);
            requestBody.put("currency",      requestDTO.getCurrencyCode());
            requestBody.put("txn_unique_id", requestDTO.getOrderId());
            requestBody.put("customer_name", requestDTO.getFirstname());
            requestBody.put("email_address", requestDTO.getEmailId());
            requestBody.put("mobile_number", requestDTO.getMobileNo());
            requestBody.put("remarks",       requestDTO.getPaymentRemarks());
            requestBody.put("payment_mode",  paymentMode);
            requestBody.put("return_url",    apiMaster.getWebhoockUrl());
            requestBody.put("cancel_url",    apiMaster.getWebhoockUrl());
            requestBody.put("callback_url",  apiMaster.getWebhoockUrl());
            // Optional notes fields — empty by default
            requestBody.put("notes_key1", "");
            requestBody.put("notes_key2", "");
            requestBody.put("notes_key3", "");
            requestBody.put("notes_key4", "");
            requestBody.put("notes_key5", "");

            HttpEntity<?> entity = new HttpEntity<>(requestBody,
                    buildShadvalHeaders(apiMaster.getSecretKey(), encPayload));
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            log.info("✅ Shadval Response [mode={}]: {}", paymentMode, response.getBody());
            return response.getBody();
        } catch (Exception e) {
            log.error("❌ Error creating Shadval session [mode={}] for orderId={}",
                    paymentMode, requestDTO.getOrderId(), e);
            return null;
        }
    }

    /**
     * Builds HDFC-specific HTTP headers with Basic auth, merchant ID, and customer ID.
     */
    private HttpHeaders buildHdfcHeaders(ApiMaster apiMaster) {
        String base64Token = Base64.getEncoder().encodeToString(apiMaster.getSecretKey().getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + base64Token);
        headers.set("x-merchantid", apiMaster.getMerchantId());
        headers.set("x-customerid", apiMaster.getClientId());
        return headers;
    }

    /**
     * Builds Shadval-specific HTTP headers with secret key auth and HMAC payload.
     */
    private HttpHeaders buildShadvalHeaders(String secretKey, String encPayload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", secretKey);
        headers.set("payload", encPayload);
        return headers;
    }
}
