package com.pv.couseae.controller;

import com.pv.couseae.entities.MerchantCryptoKeys;
import com.pv.couseae.entities.User;
import com.pv.couseae.model.PayinReqCrypto;
import com.pv.couseae.model.PayinRequestModel;
import com.pv.couseae.model.PayinResponse;
import com.pv.couseae.repos.MerchantCryptoKeysRepo;
import com.pv.couseae.repos.PayinRepo;
import com.pv.couseae.repos.PayinReqCryptoRepo;
import com.pv.couseae.services.CryptoService;
import com.pv.couseae.services.HDFCPayinService;
import com.pv.couseae.services.TransactionService;
import com.pv.couseae.services.UserService;
import com.pv.couseae.utill.CommonUtill;
import com.pv.couseae.utill.HashUtill;
import com.pv.couseae.utill.ResponseModel;
import com.pv.couseae.utill.TransactionIdGenerator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;


@Slf4j
//@CrossOrigin
@RestController
@RequestMapping("/payins")
@RequiredArgsConstructor
public class PayinController {


    private final HDFCPayinService hdfcPayinService;
    private final CommonUtill commonUtill;
    private final TransactionIdGenerator txnIdGen;
    private final HashUtill hashUtill;
    private final TransactionService txnService;
    private final PayinRepo payinRepo;
    private final PayinReqCryptoRepo payinCryptoRepo;
    private final UserService usrservice;
    private final CryptoService cryptoService;
    private final MerchantCryptoKeysRepo keysRepo;

    @GetMapping("/TestPayin")
    ResponseEntity<?> testPayin() {
        log.info("Test Payin ...");
        String map = "Payin test!!!";
        return  ResponseModel.success("Payin Test Success", map);
    }

    @PostMapping("/createOrder")
    ResponseEntity<?> createLink(@RequestBody PayinRequestModel payinReq, HttpServletRequest req) throws Exception {
        log.info(" Payin Post Order Creation....");
        String merchantAppId = req.getHeader("merchantAppId");
        String merchantSecretKey = req.getHeader("merchantSecretId");
        String hashbyClient=req.getHeader("merchantHash");
        String clientIP=commonUtill.extractClientIp(req);

       String textforhash=payinReq.getMerchantId()+payinReq.getOrderId()+payinReq.getPayableAmount();
       String serverSideHash=hashUtill.encryptHmac(textforhash,merchantSecretKey);
       if(!serverSideHash.equals(hashbyClient)){
           log.info("Payin Order Request Hash Mismatched...");
           return ResponseModel.error("Order Details Tempered...");
       }else {
           log.info("Payin Order Request Hash matched ...");
       }
        if (!merchantAppId.equals(payinReq.getAppid()))
            return ResponseModel.errorAtIntegration("Invalid APP Key");
        log.info("Client IP: "+clientIP);
        User usr = usrservice.getUserDetails(merchantAppId.trim(), merchantSecretKey.trim());
        if(usr==null){
            return ResponseModel.errorAtIntegration("Invalid Merchant Keys...");
        }else if(usr.getUserId()==null && usr.getUserId().equalsIgnoreCase(payinReq.getMerchantId())){
            return ResponseModel.errorAtIntegration("Invalid Merchant...");
        }

        if(payinRepo.findById(payinReq.getOrderId()).isPresent()){
            return  ResponseModel.error("Duplicate Order Id...");
        }

        boolean isPayinLimitReached=  txnService.payinTxnLimit(payinReq.getMerchantId(),"PAYIN",payinReq.getPayableAmount());
        log.info("In side order creation is transaction limit reached: "+!isPayinLimitReached);

        return  hdfcPayinService.getSessionResponse(merchantSecretKey,payinReq,usr);
    }
    @PostMapping("/createCryptoOrder")
    ResponseEntity<?> createCryptoOrder(@RequestBody PayinReqCrypto payinReq, HttpServletRequest req) throws Exception {
        log.info(" Crypto Post Order Creation....");
        String merchantAppId = req.getHeader("merchantAppId");
        String merchantSecretKey = req.getHeader("merchantSecretId");
        String hashbyClient=req.getHeader("merchantHash");
        String clientIP=commonUtill.extractClientIp(req);

        String textforhash=payinReq.getMerchantId()+payinReq.getOrderId()+payinReq.getFiatAmount();
        log.info("The text for hash=>"+textforhash);
        log.info("the clientside hash is =>"+hashbyClient);

        String serverSideHash=hashUtill.encryptHmac(textforhash,merchantSecretKey);
        if(!serverSideHash.equals(hashbyClient)){
            log.info("Payin Order Request Hash Mismatched...");
            return ResponseModel.error("Order Details Tempered...");
        }else {
            log.info("Payin Order Request Hash matched ...");
        }
        if (!merchantAppId.equals(payinReq.getAppid()))
            return ResponseModel.errorAtIntegration("Invalid APP Key");
        log.info("Client IP: "+clientIP);
        User usr = usrservice.getUserDetails(merchantAppId.trim(), merchantSecretKey.trim());
        if(usr==null){
            return ResponseModel.errorAtIntegration("Invalid Merchant Keys...");
        }else if(usr.getUserId()==null && usr.getUserId().equalsIgnoreCase(payinReq.getMerchantId())){
            return ResponseModel.errorAtIntegration("Invalid Merchant...");
        }

        if(payinCryptoRepo.findByOrderId(payinReq.getOrderId()).isPresent()){
            return  ResponseModel.error("Duplicate Order Id...");
        }

        Optional<MerchantCryptoKeys> opt = keysRepo.findByMerchantId(payinReq.getMerchantId());

        if (opt.isEmpty()) {
            log.warn("Merchant key not found for merchantId={}", payinReq.getMerchantId());
            return ResponseModel.error("Merchant Key not Available...");
        }

        MerchantCryptoKeys resp = opt.get();
        log.info("Merchant key found: {}", resp);

//        boolean isPayinLimitReached=  txnService.payinTxnLimit(payinReq.getMerchantId(),"PAYIN",payinReq.getPayableAmount());
//        log.info("In side order creation is transaction limit reached: "+!isPayinLimitReached);

        return  cryptoService.CreateCryptoOrder(payinReq,usr,resp.getApiKey());
    }
    @PostMapping("/CheckOrderStatus")
    ResponseEntity<?> checkOrderStatus(@RequestBody PayinRequestModel payinReq) throws Exception {
        log.info("Check Payin OrderStatus ------>");
//        String merchantAppId = req.getHeader("merchantAppId");
//        String merchantSecretKey = req.getHeader("merchantSecretId");
//        if (!merchantAppId.equals(payinReq.getAppid()))
//            return ResponseModel.errorAtIntegration("Invalid APP Key");
//        String clientIP = commonUtill.extractClientIp(req);
//        log.info("Client IP: " + clientIP);
        PayinResponse payinStatusresp =hdfcPayinService.getPaymentStatus(payinReq.getOrderId());
        return  ResponseModel.success("Order Verification Status", payinStatusresp);
    }
    @PostMapping("/payinOrderStatus")
    ResponseEntity<?> orderStatus(@RequestBody PayinRequestModel payinReq, HttpServletRequest req) throws Exception {
        log.info("Test Payin Post ");
        String merchantAppId = req.getHeader("merchantAppId");
        String merchantSecretKey = req.getHeader("merchantSecretId");
        if (!merchantAppId.equals(payinReq.getAppid()))
            return ResponseModel.errorAtIntegration("Invalid APP Key");
        String clientIP = commonUtill.extractClientIp(req);
        log.info("Client IP: " + clientIP);
        PayinResponse payinStatusresp =hdfcPayinService.getPaymentStatus(payinReq.getOrderId());
       return  ResponseModel.success("Order Verification Status", payinStatusresp);


    }



}
