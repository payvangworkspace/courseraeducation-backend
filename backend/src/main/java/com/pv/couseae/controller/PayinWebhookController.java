package com.pv.couseae.controller;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pv.couseae.Dtos.Crypto.CryptoOrderWebhookDto;
import com.pv.couseae.Dtos.HDFCWebhookResDTO;
import com.pv.couseae.Dtos.juspay.JusPayCollectWebhookDto;
import com.pv.couseae.config.SystemConfig;
import com.pv.couseae.entities.*;
import com.pv.couseae.httpServices.HttpService;
import com.pv.couseae.repos.*;
import com.pv.couseae.services.CryptoWalletService;
import com.pv.couseae.services.TransactionService;
import com.pv.couseae.services.UserService;
import com.pv.couseae.utill.HashUtill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/payinwebhook")
@RequiredArgsConstructor
public class PayinWebhookController {
    private final HashUtill shadvalUtill;
    private final PayinRepo payinRepo;
    private final PayinStatusHistoryRepo payinStatusHistoryRepo;
    private final PayinWebhookRespRepo payinWebhookRespRepo;
    private final UserService usrService;
    private final HttpService httpServ;
    private final PayinReqCryptoRepo cryptoRepo;
    private final CryptoWalletService cryptoWalletService;
    private final CryptoWebhookRespRepo cryptoWebhookRespRepo;
    private final SystemConfig systemConfig;
//    private final PayPlusClient payPlusClient;
    private final TransactionService transactionService;

    @Value("${payplus.webhook.secret}")
    private String payPlusWebhookSecret;

    // ------------------------------------------------------------------
    // Shared fee / GST helper — call ONLY on a successful (settleable) txn.
    // Sets charges, gst and netsettlementamount on the given order.
    // Caller is responsible for persisting the order afterwards.
    // ------------------------------------------------------------------
    private void applyPayinFeeAndGst(PayinRequest order, User usr, BigDecimal feeBase) {
        if (feeBase == null) feeBase = BigDecimal.ZERO;

        BigDecimal txnfee = transactionService.payinTransactionFee(
                order.getMerchantId(), "PAYIN", feeBase);
        if (txnfee == null) txnfee = BigDecimal.ZERO;
        txnfee = txnfee.setScale(2, RoundingMode.HALF_UP);

        BigDecimal gst = BigDecimal.ZERO;
        if (usr != null && usr.isPayinEnabled()) {
            gst = txnfee.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal netSettlement = feeBase.subtract(txnfee).subtract(gst)
                .setScale(2, RoundingMode.HALF_UP);

        order.setCharges(txnfee);
        order.setGst(gst);
        order.setNetsettlementamount(netSettlement);

        log.info("💰 Fee calc [{}] — base: {}, fee: {}, gst: {}, net: {}",
                order.getOrderId(), feeBase, txnfee, gst, netSettlement);
    }

    @PostMapping(value = "/sbiwebhook",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> handleSBIWebhook(@RequestParam String status,
                                              @RequestParam("signature_algorithm") String signatureAlgorithm,
                                              @RequestParam("status_id") Integer statusId,
                                              @RequestParam String signature,
                                              @RequestParam("order_id") String orderId) {
        return ResponseEntity.ok(Map.of("message", "Webhook Received Successfully !!!!"));
    }
    @PostMapping("/juspayinWebhook")
    public ResponseEntity<Object> handleJusPayWebhook(@RequestBody String reqPayload) {

        log.info("Inside the handleJusPayWebhook --->");
        log.info("Raw Payload: {}", reqPayload);

        try {
            String url="https://api.peday.money/callback/emdate";
            String activeProfiles =systemConfig.getActiveProfile();
            log.info("The active profiles are: {}",activeProfiles);
            boolean isProduction = "saloralive".equalsIgnoreCase(activeProfiles);
            if(isProduction){
                url="https://api.peday.money/callback/eMandate";

            }
            log.info("Forward Webhook to URL -->"+url);
            httpServ.postRequestWithoutHeader(url,reqPayload);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            // ✅ Step 1: Peek at type BEFORE full parse
            JsonNode root = objectMapper.readTree(reqPayload);
            String type = root.has("type") ? root.get("type").asText() : null;
            log.info("Juspay webhook type: {}", type);

            // ✅ Step 2: Route by type
            if (type == null) {
                log.warn("⚠️ Missing 'type' field in Juspay webhook, ignoring");
                return ResponseEntity.ok(Map.of("message", "Webhook Received Successfully !!!!"));
            }
//            https://api.peday.money/callback/emdate
            switch (type) {
                case "MERCHANT_CREDITED_VIA_COLLECT":
                case "MERCHANT_CREDITED_VIA_PAY": {
                    JusPayCollectWebhookDto payload =
                            objectMapper.treeToValue(root, JusPayCollectWebhookDto.class);
                    processCollectWebhook(payload, reqPayload);
                    break;
                }
                case "MERCHANT_OUTGOING_CREATE_MANDATE":
                case "MERCHANT_OUTGOING_REVOKE_MANDATE":
                case "MERCHANT_OUTGOING_UPDATE_MANDATE":
                case "MERCHANT_OUTGOING_PAUSE_MANDATE": {
//                    JusPayMandateWebhookDto payload =
//                            objectMapper.treeToValue(root, JusPayMandateWebhookDto.class);
//                    processMandateWebhook(payload, reqPayload);
                    break;
                }
                default:
                    log.warn("⚠️ Unknown Juspay webhook type: {}, ignoring", type);
                    break;
            }


        } catch (JsonProcessingException e) {
            log.error("❌ Failed to parse Juspay webhook payload: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                    "message", "Webhook Received Successfully !!!!",
                    "warning", "Parse error: " + e.getMessage()
            ));

        } catch (Exception e) {
            log.error("❌ Unexpected error in Juspay webhook: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                    "message", "Webhook Received Successfully !!!!",
                    "warning", "Processing error: " + e.getMessage()
            ));
        }

        return ResponseEntity.ok(Map.of("message", "Webhook Received Successfully !!!!"));
    }
    private void processCollectWebhook(JusPayCollectWebhookDto payload, String rawPayload) {

        log.info("merchantRequestId  : {}", payload.getMerchantRequestId());
        log.info("gatewayStatus      : {}", payload.getGatewayResponseStatus());
        log.info("gatewayResponseCode: {}", payload.getGatewayResponseCode());
        log.info("gatewayResponseMsg : {}", payload.getGatewayResponseMessage());
        log.info("payerVpa           : {}", payload.getPayerVpa());
        log.info("amount             : {}", payload.getAmount());

        PayinRequest savedOrder = payinRepo.findById(payload.getMerchantRequestId())
                .orElse(null);

        // ✅ Null check moved ABOVE the audit build — savedOrder.getAggregatorCode()
        //    would NPE for an unknown order otherwise.
        if (savedOrder == null) {
            log.warn("⚠️ Order not found for merchantRequestId: {}", payload.getMerchantRequestId());
            return;
        }

        // Save raw webhook for audit
        PayinWebhookResp webhookResp = PayinWebhookResp.builder()
                .orderid(payload.getMerchantRequestId())
                .merchantId(payload.getMerchantId())
                .acquirer(savedOrder.getAggregatorCode())
                .jsontext(rawPayload)
                .createdDate(LocalDateTime.now())
                .message("Juspay Collect Webhook - " + payload.getGatewayResponseStatus())
                .build();
        payinWebhookRespRepo.save(webhookResp);

        if ("SUCCESS".equalsIgnoreCase(savedOrder.getTransactionStatus())) {
            log.info("⏭️ Order already SUCCESS, skipping: {}", payload.getMerchantRequestId());
            return;
        }

        User usr = usrService.userById(savedOrder.getMerchantId());
        String gatewayStatus = payload.getGatewayResponseStatus();

        if ("SUCCESS".equalsIgnoreCase(gatewayStatus)) {
            savedOrder.setTransactionStatus("SUCCESS");
            savedOrder.setPayment_id(payload.getGatewayTransactionId());
            savedOrder.setResponseCode(payload.getGatewayResponseCode());
            savedOrder.setBank_ref_num(payload.getGatewayReferenceId());
            savedOrder.setSuccessAt(LocalDateTime.now());

            // Resolve fee base: prefer webhook amount, fall back to order amount
            BigDecimal feeBase = savedOrder.getAmount();
            String amtStr = payload.getAmount();
            if (amtStr != null && !amtStr.isBlank()) {
                try {
                    feeBase = new BigDecimal(amtStr.trim());
                    savedOrder.setPaidAmount(feeBase);
                } catch (NumberFormatException e) {
                    log.error("Invalid Juspay amount value: {}, falling back to order amount", amtStr, e);
                }
            } else {
                log.warn("Juspay amount is null/blank, using order amount for fee calc");
            }

            applyPayinFeeAndGst(savedOrder, usr, feeBase);
        } else if ("EXPIRED".equalsIgnoreCase(gatewayStatus)) {
            savedOrder.setTransactionStatus("EXPIRED");
        } else if ("FAILED".equalsIgnoreCase(gatewayStatus)) {
            savedOrder.setTransactionStatus("FAILED");
        } else {
            savedOrder.setTransactionStatus(gatewayStatus);
        }

        savedOrder.setStatusMessage(payload.getGatewayResponseMessage());
        savedOrder.setUpdatedAt(LocalDateTime.now());
        payinRepo.save(savedOrder);

        PayinStatusHistory statusHistory = PayinStatusHistory.builder()
                .orderId(payload.getMerchantRequestId())
                .amount(new BigDecimal(payload.getAmount()))
                .merchantId(payload.getMerchantId())
                .status(gatewayStatus)
                .remarks("Juspay Collect - " + payload.getGatewayResponseMessage())
                .createdAt(LocalDateTime.now())
                .build();
        payinStatusHistoryRepo.save(statusHistory);

        // Forward to merchant only on SUCCESS
        if ("SUCCESS".equalsIgnoreCase(gatewayStatus)) {
            if (usr != null && usr.getPayinWebhookUrl() != null && !usr.getPayinWebhookUrl().isEmpty()) {
                HashMap<String, String> hm = new HashMap<>();
                hm.put("status",               gatewayStatus);
                hm.put("orderId",              payload.getMerchantRequestId());
                hm.put("gatewayTransactionId", payload.getGatewayTransactionId());
                hm.put("gatewayReferenceId",   payload.getGatewayReferenceId());
                hm.put("amount",               payload.getAmount());
                hm.put("payerVpa",             payload.getPayerVpa());
                hm.put("payerName",            payload.getPayerName());
                hm.put("type",                 payload.getType());
                httpServ.postRequestWithoutHeader(usr.getPayinWebhookUrl(), hm);
                log.info("✅ Webhook forwarded to: {}", usr.getPayinWebhookUrl());
            }
        }
    }
    @PostMapping("/cryptoWebhook")
    public void handleCryptoPaymentWebhook(@RequestBody String reqPayload){

        log.info("Inside the crypto webhoock--->");

        log.info("The Response is --->"+reqPayload);
        ObjectMapper mapper = new ObjectMapper();
        CryptoOrderWebhookDto payload=null;
        BigDecimal filledAmount=BigDecimal.ZERO;

        try {
            payload = mapper.readValue(reqPayload, CryptoOrderWebhookDto.class);
            filledAmount = payload.getOnRampTransaction().getCryptoAmount() != null ?  payload.getOnRampTransaction().getCryptoAmount() : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Invalid webhook JSON", e);
            return;
        }
        log.info("filled amount is --->");
        // Save raw webhook for audit
        CryptoWebhookResp webhook = CryptoWebhookResp.builder()
                .cryptoOrderId(payload.getOrderId())
                .orderId(payload.getExternalOrderId())
                .merchantId(payload.getMerchant().getId())
                .message("Webhoock response")
                .jsonText(reqPayload)
                .createdDate(LocalDateTime.now())
                .build();

        cryptoWebhookRespRepo.save(webhook);


        PayinRequestCrypto savedOrder=cryptoRepo.findByOrderId(payload.getExternalOrderId()).orElse(null);
        if (savedOrder == null) {
            // handle not found case
            log.warn("Crypto order not found for orderId: {}", payload.getExternalOrderId());
            return;
        }
        if ("COMPLETED".equalsIgnoreCase(payload.getStatus()) && !savedOrder.getStatus().equalsIgnoreCase("SUCCESS")) {
            savedOrder.setStatus("SUCCESS");
            savedOrder.setCryptoType(payload.getToken());
            savedOrder.setCryptoAmount(filledAmount);
            savedOrder.setNetworkType(payload.getNetwork());
            savedOrder.setOnRampProvider(payload.getOnRampTransaction().getProvider());
            cryptoRepo.save(savedOrder);
            cryptoWalletService.updateCryptoWallet(savedOrder.getMerchantId(),payload.getToken(),payload.getNetwork(), filledAmount);
        }


    }

//    @GetMapping("/RazorPay")
//    public ResponseEntity<String> paymentCallback(
//            @RequestParam("razorpay_payment_id") String paymentId,
//            @RequestParam("razorpay_payment_link_id") String paymentLinkId,
//            @RequestParam("razorpay_payment_link_reference_id") String referenceId,
//            @RequestParam("razorpay_payment_link_status") String status,
//            @RequestParam("razorpay_signature") String signature
//    ) throws JsonProcessingException {
//
//        System.out.println("Payment ID: " + paymentId);
//        System.out.println("Payment Link ID: " + paymentLinkId);
//        System.out.println("Reference ID: " + referenceId);
//        System.out.println("Status: " + status);
//        System.out.println("Signature: " + signature);
//
//
//        Optional<PayinRequest> payinReq=payinRepo.findById(referenceId);
//        if(payinReq.isPresent() ) {
//            PayinRequest payinreqmodel = payinReq.get();
//            if ("paid".equalsIgnoreCase(status) && !payinreqmodel.getTransactionStatus().equals("SUCCESS") && status.equals("CHARGED")) {
//                payinreqmodel.setTransactionStatus("SUCCESS");
//                payinreqmodel.setStatusMessage(status);
////                    payinreqmodel.setResponseCode(statusId + "");
//                //payinreqmodel.setPayment_id();
//
//                payinreqmodel.setSuccessAt(LocalDateTime.now());
//
//                // Fee + GST — SUCCESS only. RazorPay callback carries no paid amount,
//                // so charge on the order amount.
//                User razorUsr = usrService.userById(payinreqmodel.getMerchantId());
//                applyPayinFeeAndGst(payinreqmodel, razorUsr, payinreqmodel.getAmount());
//
//                payinRepo.save(payinreqmodel);
//                ObjectMapper mapper = new ObjectMapper();
//
//                RazorPayWebhookResDTO hdfcWebhookResDTO = RazorPayWebhookResDTO.builder().
//                        orderId(referenceId).paymentLinkId(paymentLinkId).status(status)
//                        .paymentId(paymentId).signature(signature).build();
//
//                String json = mapper.writeValueAsString(hdfcWebhookResDTO);
//                PayinWebhookResp payinWebhookResp = PayinWebhookResp.builder()
//                        .orderid(payinreqmodel.getOrderId())
//                        .merchantId(payinreqmodel.getMerchantId())
//                        .acquirer(payinreqmodel.getAggregatorCode())
//                        .jsontext(json)
//                        .createdDate(LocalDateTime.now())
//                        .message("In Webhoock Response..")
//                        .build();
//                payinWebhookRespRepo.save(payinWebhookResp);
//                PayinStatusHistory payinStatusHistory = PayinStatusHistory.builder()
//                        .orderId(payinreqmodel.getOrderId())
//                        .amount(payinreqmodel.getAmount())
//                        .merchantId(payinreqmodel.getMerchantId())
//                        .status("SUCCESS")
//                        .remarks("Order Completed..")
//                        .createdAt(LocalDateTime.now())
//                        .build();
//                payinStatusHistoryRepo.save(payinStatusHistory);
//                User usr=usrService.userById(payinreqmodel.getMerchantId());
//                log.info("Usr webhoock is ---->"+usr.getPayinWebhookUrl());
//
//                if(usr!=null && usr.getPayinWebhookUrl()!=null && !usr.getPayinWebhookUrl().isEmpty()){
//                    httpServ.postRequestWithoutHeader(usr.getPayinWebhookUrl(),json);
//                    log.info("Webhook sent to  url->"+usr.getPayinWebhookUrl()+" and payload is ==>"+json);
//                }
//                return ResponseEntity.ok("Payment Successful");
//            }else{
//                ObjectMapper mapper = new ObjectMapper();
//
//                RazorPayWebhookResDTO hdfcWebhookResDTO = RazorPayWebhookResDTO.builder().
//                        orderId(referenceId).paymentLinkId(paymentLinkId).status(status)
//                        .paymentId(paymentId).signature(signature).build();
//
//                String json = mapper.writeValueAsString(hdfcWebhookResDTO);
//                PayinWebhookResp payinWebhookResp = PayinWebhookResp.builder()
//                        .orderid(payinreqmodel.getOrderId())
//                        .merchantId(payinreqmodel.getMerchantId())
//                        .acquirer(payinreqmodel.getAggregatorCode())
//                        .jsontext(json)
//                        .createdDate(LocalDateTime.now())
//                        .message("In Webhoock Response..")
//                        .build();
//                payinWebhookRespRepo.save(payinWebhookResp);
//                PayinStatusHistory payinStatusHistory = PayinStatusHistory.builder()
//                        .orderId(payinreqmodel.getOrderId())
//                        .amount(payinreqmodel.getAmount())
//                        .merchantId(payinreqmodel.getMerchantId())
//                        .status(status)
//                        .remarks("Order Completed..")
//                        .createdAt(LocalDateTime.now())
//                        .build();
//                payinStatusHistoryRepo.save(payinStatusHistory);
//            }
//
//        }
//
//
//
//        return ResponseEntity.ok("Payment Failed");
//    }
    @PostMapping(value = "/hdfcpayinwebhook",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> handleHDFCPaymentWebhook(@RequestParam String status,
                                                      @RequestParam("signature_algorithm") String signatureAlgorithm,
                                                      @RequestParam("status_id") Integer statusId,
                                                      @RequestParam String signature,
                                                      @RequestParam("order_id") String orderId) {
        try {
//            String headerid = request.getHeader("ID");
//            log.info("Payment Webhook received for --HeaderId--->" + headerid);
            // Read raw body for signature validation
            //String rawBody = new BufferedReader(request.getReader()).lines().collect(Collectors.joining("\n"));

            log.info("Raw Payment Webhook Body:" + orderId);
            // ✅ Convert raw JSON to POJO
            Optional<PayinRequest> payinReq=payinRepo.findById(orderId);
            if(payinReq.isPresent()) {
                PayinRequest payinreqmodel = payinReq.get();
                if (!payinreqmodel.getTransactionStatus().equals("SUCCESS") && status.equals("CHARGED")) {
                    payinreqmodel.setTransactionStatus("CHARGED");
                    payinreqmodel.setStatusMessage(status);
                    payinreqmodel.setResponseCode(statusId + "");
                    //payinreqmodel.setPayment_id();

                    payinreqmodel.setSuccessAt(LocalDateTime.now());

                    // Fee + GST — CHARGED is HDFC's settleable/success state.
                    // No paid amount in the webhook, so charge on the order amount.
                    User hdfcUsr = usrService.userById(payinreqmodel.getMerchantId());
                    applyPayinFeeAndGst(payinreqmodel, hdfcUsr, payinreqmodel.getAmount());

                    payinRepo.save(payinreqmodel);
                    ObjectMapper mapper = new ObjectMapper();

                    HDFCWebhookResDTO hdfcWebhookResDTO = HDFCWebhookResDTO.builder().
                            orderId(orderId).signature(signature).status(status)
                            .statusId(statusId).signatureAlgorithm(signatureAlgorithm).build();

                    String json = mapper.writeValueAsString(hdfcWebhookResDTO);
                    PayinWebhookResp payinWebhookResp = PayinWebhookResp.builder()
                            .orderid(payinreqmodel.getOrderId())
                            .merchantId(payinreqmodel.getMerchantId())
                            .acquirer(payinreqmodel.getAggregatorCode())
                            .jsontext(json)
                            .createdDate(LocalDateTime.now())
                            .message("In Webhoock Response..")
                            .build();
                    payinWebhookRespRepo.save(payinWebhookResp);
                    PayinStatusHistory payinStatusHistory = PayinStatusHistory.builder()
                            .orderId(payinreqmodel.getOrderId())
                            .amount(payinreqmodel.getAmount())
                            .merchantId(payinreqmodel.getMerchantId())
                            .status("CHARGED")
                            .remarks("Order Charged..")
                            .createdAt(LocalDateTime.now())
                            .build();
                    payinStatusHistoryRepo.save(payinStatusHistory);
                    User usr=usrService.userById(payinreqmodel.getMerchantId());
                    log.info("Usr webhoock is ---->"+usr.getPayinWebhookUrl());

                    if(usr!=null && usr.getPayinWebhookUrl()!=null && !usr.getPayinWebhookUrl().isEmpty()){
                        httpServ.postRequestWithoutHeader(usr.getPayinWebhookUrl(),json);
                        log.info("Webhook sent to  url->"+usr.getPayinWebhookUrl()+" and payload is ==>"+json);
                    }
                }
                if(payinreqmodel.getReturnUrl()!=null && !payinreqmodel.getReturnUrl().equalsIgnoreCase("")){
                    URI redirectUrl = URI.create(payinreqmodel.getReturnUrl()+"?status=" + status + "&order_id=" + orderId);
                    return ResponseEntity.status(HttpStatus.FOUND).location(redirectUrl).build();
                }
            }

//            URI redirectUrl = URI.create("https://zenithguard.in/Thankyou?status=" + status + "&order_id=" + orderId);
//            return ResponseEntity.status(HttpStatus.FOUND).location(redirectUrl).build();


        } catch (Exception e) {
            log.info("Error processing webhook: " + e.getMessage());
            return ResponseEntity.status(500).body("Error processing webhook");
        }
        return ResponseEntity.ok("Webhook received successfully");
    }

    public String decodeHS256(String jwt, String secretKey) throws Exception {

        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        JWTVerifier verifier = JWT.require(algorithm).build();

        DecodedJWT decoded = verifier.verify(jwt);

        return decoded.getPayload() != null
                ? new String(Base64.getDecoder().decode(decoded.getPayload()))
                : null;
    }
}