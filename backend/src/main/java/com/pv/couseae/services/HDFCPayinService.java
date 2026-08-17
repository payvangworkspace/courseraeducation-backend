package com.pv.couseae.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pv.couseae.Dtos.ApiRoutingContext;
import com.pv.couseae.Dtos.HdfcOrderStatusRespDTO;
import com.pv.couseae.Dtos.HdfcSessionResponseDTO;
import com.pv.couseae.Dtos.geidea.GeideaOrderResponseDto;
import com.pv.couseae.Dtos.geidea.GeideaSessionResponseDto;
import com.pv.couseae.Dtos.juspay.RegisterJusPayIntentResponse;
import com.pv.couseae.Dtos.payplus.PayPlusPayinResponseDto;
import com.pv.couseae.Dtos.payplus.PayPlusStatusResponseDto;
import com.pv.couseae.entities.*;
import com.pv.couseae.enums.TransactionStatus;
import com.pv.couseae.httpServices.GeideaClient;
import com.pv.couseae.httpServices.HttpService;
import com.pv.couseae.httpServices.PayPlusClient;
import com.pv.couseae.mappers.PayinMappers;
import com.pv.couseae.model.PayinRequestModel;
import com.pv.couseae.model.PayinResponse;
import com.pv.couseae.repos.PayinRepo;
import com.pv.couseae.repos.PayinStatusHistoryRepo;
import com.pv.couseae.repos.PayinWebhookRespRepo;
import com.pv.couseae.utill.HashUtill;
import com.pv.couseae.utill.ResponseModel;
import com.pv.couseae.utill.TransactionIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HDFCPayinService {
    private final HttpService httpService;
    private final PayinMappers payinMappers;
    private final UserService usrservice;
    private final MerchantAggregatorServ merchantAggregatorServ;
    private final PayinRepo payinRepo;
    private final TransactionIdGenerator txnIdGenerator;
    private final ApiMasterService apiMasterService;
    private final PayinStatusHistoryRepo payinStatusHistoryRepo;
    private final PayinWebhookRespRepo payinWebhookRespRepo;
    private final ObjectMapper objectMapper;
    private final PayPlusClient payPlusClient;
    private final GeideaClient geideaClient;


    public ResponseEntity<Object> getSessionResponse_old(String secretKey, PayinRequestModel payinreqmodel, User usr) throws Exception {

        try {

            log.info("🟢 Getting session response for request: {}", payinreqmodel);
            com.pv.couseae.model.PayinResponse returnRes = new PayinResponse();

            // 🧩 Filter for CREATE_SESSION & PAYIN
            String targetApiNameTemp = "CREATE_SESSION";
            if (payinreqmodel.getPaymentMode().equalsIgnoreCase("QR")){
                targetApiNameTemp = "CREATE_QR_SESSION";
            }
            final String targetApiName = targetApiNameTemp;
            String targetType = "PAYIN";

            ApiRoutingContext apiRoutingContext= apiMasterService.getApiMaster(usr,targetType, targetApiName);

            if (apiRoutingContext == null) {
                log.warn("⚠️ No matching API found ");
                return ResponseModel.error("Bank API integration not configured");
            }
            ApiMaster apiMaster = apiRoutingContext.getApiMaster();
            MerchantAggregatorMapping mapping = apiRoutingContext.getAggregatorMapping();


            // 🧩 Save initial PayinRequest in DB
            PayinRequest payinEntity = PayinRequest.builder()
                    //.orderId(txnIdGenerator.generateTransactionId())
                    .orderId(payinreqmodel.getOrderId())
                    .aggregatorTxnId(payinreqmodel.getOrderId())
                    .merchantId(usr.getUserId())
                    .aggregatorCode(mapping.getAggregatorCode())
                    .aggregatorid(mapping.getId())
                    .amount(payinreqmodel.getPayableAmount())
                    .currency(payinreqmodel.getCurrencyCode())
                    .paymentMode(payinreqmodel.getPaymentMode())
                    .transactionStatus("INITIALIZED")
                    .firstName(payinreqmodel.getFirstname())
                    .lastName(payinreqmodel.getLastname())
                    .customerEmail(payinreqmodel.getEmailId())
                    .customerMobile(payinreqmodel.getMobileNo())
                    .paymentdesc(payinreqmodel.getPaymentRemarks())
                    .settled(false)
                    .transactionStatus(TransactionStatus.INITIATED.toString())
                    .statusMessage("Session Created")
                    .initiatedAt(LocalDateTime.now())
                    .createdBy(usr.getUserId())
                    .createdOn(LocalDateTime.now())
                    .build();


            PayinRequest savedPayin = payinRepo.save(payinEntity);
            log.info("💾 PayinRequest saved successfully with OrderId={}", savedPayin.getOrderId());
            PayinStatusHistory payinStatusHistory = PayinStatusHistory.builder()
                    .orderId(payinreqmodel.getOrderId())
                    .amount(payinreqmodel.getPayableAmount())
                    .merchantId(usr.getUserId())
                    .status("Initiated")
                    .remarks("Order initiated..")
                    .createdAt(LocalDateTime.now())
                    .build();
            payinStatusHistoryRepo.save(payinStatusHistory);

            // 🧩 Call HDFC API to create payment session (non-reactive)
            String jsonResponse = null;
            if (mapping.getAggregatorCode() != null && mapping.getAggregatorCode().equalsIgnoreCase("HDFC")) {
                jsonResponse = httpService.createHDFCPaymentSession(apiMaster, payinreqmodel);

                HdfcSessionResponseDTO responseDTO = payinMappers.parseResponse(jsonResponse);
                returnRes.setEmailId(payinreqmodel.getEmailId());
                returnRes.setMobileNo(payinreqmodel.getMobileNo());
                returnRes.setOrderId(payinreqmodel.getOrderId());
                returnRes.setPaymentlink(responseDTO.getPayment_links().getWeb());
                returnRes.setLinkexpirytime(responseDTO.getPayment_links().getExpiry() + "");
                returnRes.setStatusCode(responseDTO.getStatus());
                returnRes.setMessage(responseDTO.getSdk_payload().getPayload().getDescription());
                PayinStatusHistory payinStatusHistory1 = PayinStatusHistory.builder()
                        .orderId(payinreqmodel.getOrderId())
                        .amount(payinreqmodel.getPayableAmount())
                        .merchantId(usr.getUserId())
                        .status("Processing")
                        .remarks("Payment link generated")
                        .createdAt(LocalDateTime.now())
                        .build();
                payinStatusHistoryRepo.save(payinStatusHistory1);

                log.info("✅ Payment URL: {}", responseDTO.getPayment_links().getWeb());

                // 🧩 Update Payin Request with payment link info
                savedPayin.setPaymentLink(responseDTO.getPayment_links().getWeb());
                savedPayin.setTransactionStatus("PROCESSING");
                savedPayin.setStatusMessage("Session created successfully");
                savedPayin.setUpdatedAt(LocalDateTime.now());
            }


            if (jsonResponse == null || jsonResponse.isEmpty()) {
                log.error("❌ Empty or null response received from session API");
                return null;
            }
            payinRepo.save(savedPayin);
            PayinWebhookResp payinWebhookResp = PayinWebhookResp.builder()
                    .orderid(payinreqmodel.getOrderId())
                    .merchantId(usr.getUserId())
                    .acquirer(mapping.getAggregatorCode())
                    .jsontext(jsonResponse)
                    .createdDate(LocalDateTime.now())
                    .message("In Link Genration..")
                    .build();
            payinWebhookRespRepo.save(payinWebhookResp);


            log.info("🔁 PayinRequest updated with session info for OrderId={}", savedPayin.getOrderId());

            return ResponseModel.success("Order Data",returnRes);

        } catch (Exception e) {
            log.error("❌ Failed to parse session response JSON", e);
            return null;
        }
    }

    public ResponseEntity<Object> getSessionResponse(
            String secretKey,
            PayinRequestModel payinReqModel,
            User user) throws Exception {

        log.info("🟢 Getting session response for request: {}", payinReqModel);

        try {
            // ── Resolve API routing ──────────────────────────────────────────
            ApiRoutingContext routingContext = resolveApiRoutingContext(payinReqModel, user);
            if (routingContext == null) {
                log.warn("⚠️ No matching API found for user={}", user.getUserId());
                return ResponseModel.error("Bank API integration not configured");
            }

            ApiMaster              apiMaster = routingContext.getApiMaster();
            MerchantAggregatorMapping mapping = routingContext.getAggregatorMapping();
            String            aggregatorCode  = mapping.getAggregatorCode();

            // ── Persist initial transaction ──────────────────────────────────
            PayinRequest savedPayin = createAndSaveInitialPayinRequest(payinReqModel, user, mapping);
            saveStatusHistory(payinReqModel, user, "Initiated", "Order initiated..");

            // ── Delegate to aggregator-specific handler ──────────────────────
            PayinResponse payinResponse = switch (aggregatorCode.toUpperCase()) {
                case "HDFC"    -> handleHdfc(apiMaster, mapping, payinReqModel, user, savedPayin);
                case "PAYPLUS" -> handlePayPlus(apiMaster, mapping, payinReqModel, user, savedPayin);
                case "GEIDEA"  -> handleGeidea(apiMaster, mapping, payinReqModel, user, savedPayin);
                default        -> throw new IllegalArgumentException("Unsupported aggregator: " + aggregatorCode);
            };

            // ── Persist updated state & raw webhook log ──────────────────────
            payinRepo.save(savedPayin);


            log.info("🔁 Session finalised for orderId={}", savedPayin.getOrderId());
            return ResponseModel.success("Order Data", payinResponse);

        } catch (IllegalArgumentException e) {
            log.error("❌ Configuration error: {}", e.getMessage());
            return ResponseModel.error(e.getMessage());

        } catch (Exception e) {
            log.error("❌ Failed to process session response", e);
            return ResponseModel.error("Internal error while processing payment session");
        }
    }


// ── Private helpers ──────────────────────────────────────────────────────────

    private ApiRoutingContext resolveApiRoutingContext(PayinRequestModel req, User user) {
        boolean isQr       = "QR".equalsIgnoreCase(req.getPaymentMode());
        boolean isIntent = "INTENT".equalsIgnoreCase(req.getPaymentMode());

        String apiName = isQr     ? "CREATE_QR_SESSION"
                : isIntent ? "CREATE_INTENT_SESSION"
                :            "CREATE_SESSION";


        return apiMasterService.getApiMaster(user, "PAYIN", apiName);
    }

    private PayinRequest createAndSaveInitialPayinRequest(
            PayinRequestModel req, User user, MerchantAggregatorMapping mapping) {

        PayinRequest entity = PayinRequest.builder()
                .orderId           (req.getOrderId())
                .aggregatorTxnId   (req.getOrderId())
                .merchantId        (user.getUserId())
                .aggregatorCode    (mapping.getAggregatorCode())
                .aggregatorid      (mapping.getId())
                .amount            (req.getPayableAmount())
                .currency          (req.getCurrencyCode())
                .paymentMode       (req.getPaymentMode())
                .firstName         (req.getFirstname())
                .lastName          (req.getLastname())
                .customerEmail     (req.getEmailId())
                .customerMobile    (req.getMobileNo())
                .paymentdesc       (req.getPaymentRemarks())
                .settled           (false)
                .transactionStatus (TransactionStatus.INITIATED.toString())
                .statusMessage     ("Session Created")
                .initiatedAt       (LocalDateTime.now())
                .createdBy         (user.getUserId())
                .createdOn         (LocalDateTime.now())
                .returnUrl(req.getReturn_url())
                .build();

        PayinRequest saved = payinRepo.save(entity);
        log.info("💾 PayinRequest saved | orderId={}", saved.getOrderId());
        return saved;
    }

    private void saveStatusHistory(
            PayinRequestModel req, User user, String status, String remarks) {

        payinStatusHistoryRepo.save(
                PayinStatusHistory.builder()
                        .orderId   (req.getOrderId())
                        .amount    (req.getPayableAmount())
                        .merchantId(user.getUserId())
                        .status    (status)
                        .remarks   (remarks)
                        .createdAt (LocalDateTime.now())
                        .build()
        );
    }

    private void saveWebhookLog(PayinRequestModel req, User user,
                                MerchantAggregatorMapping mapping, String rawJson) {

        payinWebhookRespRepo.save(PayinWebhookResp.builder()
                .orderid     (req.getOrderId())
                .merchantId  (user.getUserId())
                .acquirer    (mapping.getAggregatorCode())
                .jsontext    (rawJson)
                .createdDate (LocalDateTime.now())
                .message     ("In Link Generation..")
                .build()
        );
    }

// ── Aggregator handlers ──────────────────────────────────────────────────────

    private PayinResponse handleHdfc(
            ApiMaster apiMaster, MerchantAggregatorMapping mapping,
            PayinRequestModel req, User user, PayinRequest savedPayin) throws Exception {

        String              rawJson     = httpService.createHDFCPaymentSession(apiMaster, req);
        HdfcSessionResponseDTO hdfcResp = payinMappers.parseResponse(rawJson);

        savedPayin.setPaymentLink      (hdfcResp.getPayment_links().getWeb());
        savedPayin.setTransactionStatus("PROCESSING");
        savedPayin.setStatusMessage    ("Session created successfully");
        savedPayin.setUpdatedAt        (LocalDateTime.now());
        //savedPayin.setRawJsonResponse  (rawJson);

        saveStatusHistory(req, user, "Processing", "Payment link generated");
        log.info("✅ HDFC payment URL generated | orderId={}", req.getOrderId());
        String jsonResponse = objectMapper.writeValueAsString(hdfcResp);
        saveWebhookLog(req, user, mapping, jsonResponse);
        return PayinResponse.builder()
                .emailId        (req.getEmailId())
                .mobileNo       (req.getMobileNo())
                .orderId        (req.getOrderId())
                .paymentlink    (hdfcResp.getPayment_links().getWeb())
                .linkexpirytime (String.valueOf(hdfcResp.getPayment_links().getExpiry()))
                .statusCode     (hdfcResp.getStatus())
                .message        (hdfcResp.getSdk_payload().getPayload().getDescription())
                .build();
    }


    private PayinResponse handlePayPlus(
            ApiMaster apiMaster, MerchantAggregatorMapping mapping,
            PayinRequestModel req, User user, PayinRequest savedPayin) throws Exception {

        if(user.getUserId().equalsIgnoreCase("jsspay26@gmail.com")){
            //apiMaster.setSecretKey("");
        }
        PayPlusPayinResponseDto resp = payPlusClient.createPayin(apiMaster, req);
        log.info("🟢 PayPlus createPayin response: {}", resp);

        String jsonResponse = objectMapper.writeValueAsString(resp);
        savedPayin.setUpdatedAt(LocalDateTime.now());
        saveWebhookLog(req, user, mapping, jsonResponse);

        if (resp != null && resp.isSuccess() && resp.getData() != null) {
            PayPlusPayinResponseDto.Data data = resp.getData();

            Map<String, String> upiApps = (data.getUpiIntent() != null && data.getUpiIntent().getApps() != null)
                    ? data.getUpiIntent().getApps()
                    : Collections.emptyMap();

            savedPayin.setTransactionStatus("PROCESSING");
            savedPayin.setGatewayReferenceId(data.getOrderId());
            savedPayin.setStatusMessage(resp.getMessage());
            savedPayin.setTxnId(data.getTxnId());
// no setPaymentLink — this aggregator has no hosted URL

            saveStatusHistory(req, user, "PROCESSING", "PayPlus order created successfully");
            log.info("✅ PayPlus payin created | orderId={}, apps={}",
                    data.getOrderId(), upiApps.keySet());

            return PayinResponse.builder()
                    .emailId      (req.getEmailId())
                    .mobileNo     (req.getMobileNo())
                    .orderId      (req.getOrderId())
                    .upiIntentApps(upiApps)                 // paytm / cred / phonepe / bharatpe
                    .paymentlink(data.getPaymentUrl())
                    .statusCode   (data.getStatus())        // "processing"
                    .message      (resp.getMessage())
                    .ordRequestId (data.getOrderId())
                    .amount       (String.valueOf(savedPayin.getAmount())) // was data.getAmount() → null
                    .build();

        } else {
            String errorMsg = resp != null ? resp.getMessage() : "PayPlus payin creation failed";
            log.error("❌ PayPlus createPayin failed: {}", errorMsg);

            savedPayin.setTransactionStatus("FAILED");
            savedPayin.setStatusMessage    ("PayPlus payin creation failed");

            return PayinResponse.builder()
                    .statusCode("FAILURE")
                    .message   (errorMsg)
                    .build();
        }
    }

    private PayinResponse handleGeidea(
            ApiMaster apiMaster, MerchantAggregatorMapping mapping,
            PayinRequestModel req, User user, PayinRequest savedPayin) throws Exception {

        GeideaSessionResponseDto resp = geideaClient.createSession(apiMaster, req);
        log.info("🟢 Geidea createSession response: {}", resp);

        String jsonResponse = objectMapper.writeValueAsString(resp);
        savedPayin.setUpdatedAt(LocalDateTime.now());
        saveWebhookLog(req, user, mapping, jsonResponse);

        // Geidea returns a session object with an id; success indicated by responseCode "000".
        // Adjust these getters to match your GeideaSessionResponseDto shape.
        if (resp != null && resp.getSession() != null && resp.getSession().getId() != null) {

            String sessionId = resp.getSession().getId();
            String hostedUrl = geideaClient.hostedCheckoutUrl(sessionId);

            savedPayin.setTransactionStatus("PROCESSING");
            savedPayin.setGatewayReferenceId(sessionId);
            savedPayin.setStatusMessage(resp.getResponseMessage());
            savedPayin.setPaymentLink(hostedUrl);

            saveStatusHistory(req, user, "PROCESSING", "Geidea session created successfully");
            log.info("✅ Geidea session created | orderId={}, sessionId={}", req.getOrderId(), sessionId);

            return PayinResponse.builder()
                    .emailId     (req.getEmailId())
                    .mobileNo    (req.getMobileNo())
                    .orderId     (req.getOrderId())
                    .paymentlink (hostedUrl)
                    .statusCode  (resp.getResponseCode())
                    .message     (resp.getResponseMessage())
                    .ordRequestId(sessionId)
                    .amount      (String.valueOf(savedPayin.getAmount()))
                    .build();

        } else {
            String errorMsg = resp != null ? resp.getResponseMessage() : "Geidea session creation failed";
            log.error("❌ Geidea createSession failed: {}", errorMsg);

            savedPayin.setTransactionStatus("FAILED");
            savedPayin.setStatusMessage("Geidea session creation failed");

            return PayinResponse.builder()
                    .statusCode("FAILURE")
                    .message   (errorMsg)
                    .build();
        }
    }

    private Map<String, String> buildUpiDeeplinks(RegisterJusPayIntentResponse.Payload p) throws Exception {
        String params = "pa="  + encode(p.getPayeeVpa())
                + "&pn="  + encode(p.getPayeeName())
                + "&mc="  + encode(p.getPayeeMcc())
                + "&tid=" + encode(p.getGatewayTransactionId())
                + "&tr="  + encode(p.getOrderId())
                + "&am="  + encode(p.getAmount())
                + "&cu="  + encode(p.getCurrency() != null ? p.getCurrency() : "INR")
                + "&mode=00";

        if (p.getRemarks() != null) {
            params += "&tn=" + encode(p.getRemarks());
        }

        Map<String, String> links = new LinkedHashMap<>();
        links.put("generic",  "upi://pay?"          + params);
        links.put("gpay",     "tez://upi/pay?"       + params);
        links.put("phonepe",  "phonepe://pay?"        + params);
        links.put("paytm",    "paytmmp://pay?"        + params);
        links.put("cred",     "credpay://upi/pay?"    + params);
        links.put("bhim",     "bhim://pay?"           + params);
        return links;
    }

    private String encode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value != null ? value : "", StandardCharsets.UTF_8.toString());
    }

    public PayinResponse getPaymentStatus(String orderid) throws Exception {
        PayinResponse returnRes = new PayinResponse();
        try {

            log.info("🟢 Getting payment status for request: {}", orderid);

            Optional<PayinRequest> payin = payinRepo.findById(orderid);
            if (payin.isPresent()) {
                PayinRequest req = payin.get();
                log.warn("⚠️ OrderId found for : {}", orderid);


                // 🧩 Filter for CREATE_SESSION & PAYIN
                String targetApiName = "GET_ORDER_STATUS";
                String targetType = "PAYIN";

                if(req.getPaymentMode().equalsIgnoreCase("QR")){
                    targetApiName = "GET_QR_ORDER_STATUS";
                }


                ApiMaster apiMaster=apiMasterService.getApiMasterByAggregatorId(req.getAggregatorid(),targetApiName,targetType);
                if (apiMaster == null) {
                    log.warn("⚠️ No matching API found for name={} and type={} under aggregator={}",
                            targetApiName, targetType, req.getAggregatorCode());
                    return null;
                }

                if (req.getAggregatorCode() != null && req.getAggregatorCode().equalsIgnoreCase("HDFC")) {
                    String stsresp = httpService.getHdfcOrderStatus(apiMaster, req.getOrderId());
                    log.info("🟢 Payment Status Response: {}", stsresp);
                    HdfcOrderStatusRespDTO statusDTO = payinMappers.parseHdfcOrderStatusResponse(stsresp);
                    // Response Object

                    returnRes.setStatusCode(statusDTO.getStatus());
                    returnRes.setMessage("");
                    returnRes.setOrdTransactionId(statusDTO.getTxn_id());
                    returnRes.setTxnMethod(statusDTO.getPayment_method());
                    returnRes.setTxnType(statusDTO.getPayment_method_type());
                    returnRes.setOrdRequestId(statusDTO.getOrder_id());
                    returnRes.setEmailId(statusDTO.getCustomer_email());
                    returnRes.setMobileNo(statusDTO.getCustomer_phone());
                    returnRes.setAmount(String.valueOf(statusDTO.getAmount()));
                    // Card Details
                    if (statusDTO.getCard() != null) {
                        returnRes.setCardBrand(statusDTO.getCard().getCard_brand());
                        returnRes.setCardType(statusDTO.getCard().getCard_type());
                        returnRes.setCardIssuer(statusDTO.getCard().getCard_issuer());
//                            returnRes.setCardLastFourDigits(statusDTO.getCard().getLast_four_digits());
                        returnRes.setCardHolderName(statusDTO.getCard().getName_on_card());
                        returnRes.setCardIsin(statusDTO.getCard().getCard_isin());
                    }
                    // Gateway Details
                    if (statusDTO.getPayment_gateway_response() != null) {
                        returnRes.setRrn(statusDTO.getPayment_gateway_response().getRrn());
                        returnRes.setMessage(statusDTO.getPayment_gateway_response().getResp_message());
                    }
                    // Update Database
                    if (req.getTransactionStatus() != null && !req.getTransactionStatus().equalsIgnoreCase("SUCCESS")) {
                        req.setTransactionStatus(statusDTO.getStatus());
                        req.setPayment_id(statusDTO.getTxn_id());
                        req.setTxnId(statusDTO.getTxn_id());
                        req.setTxnUuid(statusDTO.getTxn_uuid());
                        req.setPaymentMethod(statusDTO.getPayment_method());
                        req.setPaymentMethodType(statusDTO.getPayment_method_type());
                        req.setAuthType(statusDTO.getAuth_type());
                        req.setCustomerEmail(statusDTO.getCustomer_email());
                        req.setCustomerMobile(statusDTO.getCustomer_phone());
                        req.setAggregatorTxnId(statusDTO.getId());
                        req.setAmountRefunded(statusDTO.getAmount_refunded());
                        req.setEffectiveAmount(statusDTO.getEffective_amount());
                        req.setRefunded(statusDTO.isRefunded());
                        req.setPayment_date(statusDTO.getLast_updated());
                        if (statusDTO.getCard() != null) {
                            req.setExpiryYear(statusDTO.getCard().getExpiry_year());
                            req.setExpiryMonth(statusDTO.getCard().getExpiry_month());
                            req.setCardReference(statusDTO.getCard().getCard_reference());
                            req.setSavedToLocker(statusDTO.getCard().getSaved_to_locker());
                            req.setCardHolderName(statusDTO.getCard().getName_on_card());
                            req.setCardIssuer(statusDTO.getCard().getCard_issuer());
                            req.setCardLastFourDigits(statusDTO.getCard().getLast_four_digits());
                            req.setUsingSavedCard(statusDTO.getCard().getUsing_saved_card());
                            req.setCardFingerprint(statusDTO.getCard().getCard_fingerprint());
                            req.setCardIsin(statusDTO.getCard().getCard_isin());
                            req.setCardType(statusDTO.getCard().getCard_type());
                            req.setCardBrand(statusDTO.getCard().getCard_brand());
                            req.setCardIssuerCountry(statusDTO.getCard().getCard_issuer_country());
                            req.setJuspayBankCode(statusDTO.getCard().getJuspay_bank_code());
                            req.setExtendedCardType(statusDTO.getCard().getExtended_card_type());
                            req.setPaymentAccountReference(statusDTO.getCard().getPayment_account_reference());
                            req.setCardSubTypeCategory(statusDTO.getCard().getCard_sub_type_category());
                        }
                        if (statusDTO.getPayment_gateway_response() != null) {
                            req.setBank_ref_num(statusDTO.getPayment_gateway_response().getRrn());
                            req.setRrn(statusDTO.getPayment_gateway_response().getRrn());
                            req.setAuthCode(statusDTO.getPayment_gateway_response().getAuth_id_code());
                            req.setGatewayTxnId(statusDTO.getPayment_gateway_response().getEpg_txn_id());
                            req.setGatewayRespCode(statusDTO.getPayment_gateway_response().getResp_code());
                            req.setGatewayRespMessage(statusDTO.getPayment_gateway_response().getResp_message());
                            req.setGatewayMerchantId(statusDTO.getPayment_gateway_response().getGateway_merchant_id());
                        }
                        req.setUpdatedAt(LocalDateTime.now());
                        if ("CHARGED".equalsIgnoreCase(statusDTO.getStatus())) {
                            req.setTransactionStatus("SUCCESS");
                            req.setSuccessAt(LocalDateTime.now());
                        }
                        payinRepo.save(req);
                    }

                    PayinStatusHistory payinStatusHistory1 = PayinStatusHistory.builder()
                            .orderId(req.getOrderId())
                            .amount(req.getAmount())
                            .merchantId(req.getMerchantId())
                            .status(statusDTO.getStatus())
                            .remarks("Order Status Check..")
                            .createdAt(LocalDateTime.now())
                            .build();
                    payinStatusHistoryRepo.save(payinStatusHistory1);

                    PayinWebhookResp payinWebhookResp = PayinWebhookResp.builder()
                            .orderid(req.getOrderId())
                            .merchantId(req.getMerchantId())
                            .acquirer(apiMaster.getAggregatorCode())
                            .jsontext(stsresp)
                            .createdDate(LocalDateTime.now())
                            .message("In Order Check Status ..")
                            .build();
                    payinWebhookRespRepo.save(payinWebhookResp);

                    return returnRes;
                }  else if (apiMaster.getAggregatorCode() != null && apiMaster.getAggregatorCode().equalsIgnoreCase("JUSPAY")) {

                }else if (apiMaster.getAggregatorCode() != null && apiMaster.getAggregatorCode().equalsIgnoreCase("PAYPLUS")) {
                    return handlePayPlusStatus(apiMaster, req, returnRes);
                } else if (apiMaster.getAggregatorCode() != null && apiMaster.getAggregatorCode().equalsIgnoreCase("GEIDEA")) {
                    return handleGeideaStatus(apiMaster, req, returnRes);
                }
                return returnRes;

            }else{
                log.warn("⚠️ OrderId Not found for : {}", orderid);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return returnRes;
    }

    private PayinResponse handlePayPlusStatus(
            ApiMaster apiMaster, PayinRequest req, PayinResponse returnRes) throws Exception {

        if(req.getMerchantId().equalsIgnoreCase("jsspay26@gmail.com")){
            //apiMaster.setSecretKey("");
        }
        PayPlusStatusResponseDto resp = payPlusClient.checkStatus(apiMaster, req.getOrderId());
        log.info("🟢 PayPlus checkStatus response: {}", resp);

        returnRes.setOrdRequestId(req.getOrderId());

        if (resp != null && resp.isSuccess() && resp.getData() != null) {
            PayPlusStatusResponseDto.StatusData data = resp.getData();

            returnRes.setStatusCode      (data.getStatus());
            returnRes.setMessage         (resp.getMessage());
            returnRes.setAmount          (data.getAmount());
            returnRes.setOrdTransactionId(data.getUtr());

            // Update DB only if not already SUCCESS
            if (req.getTransactionStatus() != null
                    && !req.getTransactionStatus().equalsIgnoreCase("SUCCESS")) {
                req.setTransactionStatus(data.getStatus());
                payinRepo.save(req);
            }

            PayinStatusHistory history = PayinStatusHistory.builder()
                    .orderId(req.getOrderId())
                    .amount(req.getAmount())
                    .merchantId(req.getMerchantId())
                    .status(data.getStatus())
                    .remarks("PayPlus Order Status Check..")
                    .createdAt(LocalDateTime.now())
                    .build();
            payinStatusHistoryRepo.save(history);

            PayinWebhookResp webhook = PayinWebhookResp.builder()
                    .orderid(req.getOrderId())
                    .merchantId(req.getMerchantId())
                    .acquirer(apiMaster.getAggregatorCode())
                    .jsontext(objectMapper.writeValueAsString(resp))
                    .createdDate(LocalDateTime.now())
                    .message("PayPlus Status Check")
                    .build();
            payinWebhookRespRepo.save(webhook);

        } else {
            returnRes.setStatusCode("FAILURE");
            returnRes.setMessage(resp != null ? resp.getMessage() : "No response from PayPlus");

            req.setTransactionStatus("FAILURE");
            payinRepo.save(req);

            PayinStatusHistory history = PayinStatusHistory.builder()
                    .orderId(req.getOrderId())
                    .amount(req.getAmount())
                    .merchantId(req.getMerchantId())
                    .status("FAILURE")
                    .remarks("PayPlus Status Check Failed")
                    .createdAt(LocalDateTime.now())
                    .build();
            payinStatusHistoryRepo.save(history);
        }

        return returnRes;
    }

    private PayinResponse handleGeideaStatus(
            ApiMaster apiMaster, PayinRequest req, PayinResponse returnRes) throws Exception {

        GeideaOrderResponseDto resp = geideaClient.checkStatus(apiMaster, req.getOrderId());
        log.info("🟢 Geidea checkStatus response: {}", resp);

        returnRes.setOrdRequestId(req.getOrderId());

        // Adjust getters to your GeideaOrderResponseDto shape (order.status / order.amount etc.)
        if (resp != null && resp.getOrder() != null) {
            GeideaOrderResponseDto.Order order = resp.getOrder();

            returnRes.setStatusCode(order.getStatus());
            returnRes.setMessage(resp.getResponseMessage());
            returnRes.setAmount(String.valueOf(order.getAmount()));

            if (req.getTransactionStatus() != null
                    && !req.getTransactionStatus().equalsIgnoreCase("SUCCESS")) {
                req.setTransactionStatus(order.getStatus());
                // Geidea "Paid"/"Success" → mark SUCCESS; adjust to Geidea's actual status vocabulary
                if ("PAID".equalsIgnoreCase(order.getStatus())
                        || "SUCCESS".equalsIgnoreCase(order.getStatus())) {
                    req.setTransactionStatus("SUCCESS");
                    req.setSuccessAt(LocalDateTime.now());
                }
                payinRepo.save(req);
            }

            PayinStatusHistory history = PayinStatusHistory.builder()
                    .orderId(req.getOrderId())
                    .amount(req.getAmount())
                    .merchantId(req.getMerchantId())
                    .status(order.getStatus())
                    .remarks("Geidea Order Status Check..")
                    .createdAt(LocalDateTime.now())
                    .build();
            payinStatusHistoryRepo.save(history);

            PayinWebhookResp webhook = PayinWebhookResp.builder()
                    .orderid(req.getOrderId())
                    .merchantId(req.getMerchantId())
                    .acquirer(apiMaster.getAggregatorCode())
                    .jsontext(objectMapper.writeValueAsString(resp))
                    .createdDate(LocalDateTime.now())
                    .message("Geidea Status Check")
                    .build();
            payinWebhookRespRepo.save(webhook);

        } else {
            returnRes.setStatusCode("FAILURE");
            returnRes.setMessage(resp != null ? resp.getResponseMessage() : "No response from Geidea");
        }

        return returnRes;
    }
}