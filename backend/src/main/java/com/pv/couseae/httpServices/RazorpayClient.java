package com.pv.couseae.httpServices;

import com.pv.couseae.entities.ApiMaster;
import com.pv.couseae.model.PayinRequestModel;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class RazorpayClient {

    // ─── Constants ────────────────────────────────────────────────────────────
    private static final String KEY_ID         = "rzp_test_RFPYKSNhOLTQcq";
    private static final String KEY_SECRET     = "k3QwtE4zoIrOy19pP51OONdO";
//
    private static final String BASE_URL       = "https://api.razorpay.com/v1";
//    private static final String ORDERS_URL     = BASE_URL + "/orders";
//    private static final String PAY_LINKS_URL  = BASE_URL + "/payment_links/";
//    private static final String CUSTOMERS_URL  = BASE_URL + "/customers";
//    private static final String QR_CODES_URL   = BASE_URL + "/payments/qr_codes";

    private static final String CURRENCY_INR   = "INR";
    private static final int    EXPIRY_MINUTES = 30;

    // ─── Shared RestTemplate ──────────────────────────────────────────────────
    private final RestTemplate restTemplate = new RestTemplate();


    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC METHODS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a Razorpay order.
     *
     * @param orderId Merchant order ID (used as receipt)
     * @param amount  Amount in paise (e.g. 5000 = ₹50)
     * @param note    Note to attach to the order
     */
    public String createOrder(String orderId, BigDecimal amount, String note) {
        Map<String, Object> request = new HashMap<>();
        request.put("amount", amount);         // pass actual amount, not hardcoded 5000
        request.put("currency", CURRENCY_INR);
        request.put("receipt", orderId);

        Map<String, String> notes = new HashMap<>();
        notes.put("key1", note);
        notes.put("key2", "value2");
        request.put("notes", notes);

        String response = post(BASE_URL + "/orders", request);
        System.out.println("CreateOrder Response: " + response);
        return response;
    }


    public String createOrderLink(ApiMaster apiMaster, PayinRequestModel payinreqmodel) {
        Map<String, Object> request = new HashMap<>();
        request.put("amount", payinreqmodel.getPayableAmount());
        request.put("currency", CURRENCY_INR);
        request.put("upi_link", "false");
        request.put("expire_by", generateExpiry(EXPIRY_MINUTES));
        request.put("reference_id", payinreqmodel.getOrderId());
        request.put("description", payinreqmodel.getPaymentRemarks());

        Map<String, Object> customer = new HashMap<>();
        customer.put("name", payinreqmodel.getFirstname());
        customer.put("contact", payinreqmodel.getMobileNo());
        customer.put("email", payinreqmodel.getEmailId());
        request.put("customer", customer);

        Map<String, Object> notify = new HashMap<>();
        notify.put("email", true);
        request.put("notify", notify);

        Map<String, Object> notes = new HashMap<>();
        notes.put("policy_name", "Life Insurance Policy");
        request.put("notes", notes);

        request.put("callback_url", apiMaster.getWebhoockUrl());
        request.put("callback_method", "get");

        return post(BASE_URL + "/payment_links/", request);
    }

    /**
     * Creates or fetches a Razorpay customer.
     *
     * @param customerName Customer full name
     * @param custMobNo    Customer mobile number
     * @param email        Customer email address
     */
    public String createCustomer(String customerName, String custMobNo, String email) {
        Map<String, Object> request = new HashMap<>();
        request.put("name", customerName);
        request.put("contact", custMobNo);
        request.put("email", email);
        request.put("fail_existing", 0);

        Map<String, Object> notes = new HashMap<>();
        notes.put("notes_key_1", "Tea, Earl Grey, Hot");
        notes.put("notes_key_2", "Tea, Earl Grey… decaf.");
        request.put("notes", notes);

        return post(BASE_URL + "/customers", request);
    }

    /**
     * Creates a UPI QR code for a fixed payment amount.
     *
     * @param customerId  Razorpay customer ID
     * @param amount      Fixed payment amount in paise
     * @param description Description for QR code
     */
    public String createQrCode(String customerId, BigDecimal amount, String description) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "upi_qr");
        request.put("name", "Store Front Display");
        request.put("usage", "single_use");   // options: single_use, multiple_use
        request.put("fixed_amount", true);
        request.put("payment_amount", amount);
        request.put("description", description);
        request.put("customer_id", customerId);
        request.put("close_by", generateExpiry(EXPIRY_MINUTES));

        Map<String, Object> notes = new HashMap<>();
        notes.put("purpose", "Test UPI QR Code notes");
        request.put("notes", notes);

        return post(BASE_URL + "/payments/qr_codes", request);
    }

    /**
     * Closes an active QR code.
     *
     * @param qrId Razorpay QR code ID to close
     */
    public String closeQrCode(String qrId) {
        String url = BASE_URL + "/payments/qr_codes/" + qrId + "/close";
        HttpEntity<String> entity = new HttpEntity<>(buildAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return response.getBody();
    }

    /**
     * Generates a Unix timestamp for expiry (current time + given minutes).
     *
     * @param minutes Number of minutes from now
     * @return Unix timestamp in seconds
     */
    public long generateExpiry(int minutes) {
        return (System.currentTimeMillis() / 1000) + ((long) minutes * 60);
    }


    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds HTTP headers with Basic Auth and JSON content type.
     */
    private HttpHeaders buildAuthHeaders() {
        String auth = KEY_ID + ":" + KEY_SECRET;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + encodedAuth);
        return headers;
    }

    /**
     * Executes a POST request to the given URL with the provided request body.
     *
     * @param url     Target API endpoint
     * @param request Request body as a Map
     * @return Response body as String
     */
    private String post(String url, Map<String, Object> request) {
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, buildAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return response.getBody();
    }
}
