package com.pv.couseae.controller;

import com.pv.couseae.Dtos.TransactionFeeReq;
import com.pv.couseae.entities.FeeRule;
import com.pv.couseae.entities.LimitRule;
import com.pv.couseae.services.FeeAndLimitRuleService;
import com.pv.couseae.services.TransactionService;
import com.pv.couseae.utill.ResponseModel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/FeeLimitRule")
@AllArgsConstructor
public class FeeRuleController {

    private final FeeAndLimitRuleService feeLimitRuleService;
    private final TransactionService transactionService;

    @PostMapping("/GetFeeRules")
    public ResponseEntity<?> getAllFeeRules(){
        log.info("Get All Fee Rules");
        return ResponseModel.success("Fee Rules", feeLimitRuleService.getAllFeeRules());
    }
    @PostMapping("/GetLimitRules")
    public ResponseEntity<?> getAllLimitRules(){
        log.info("Get All Limit Rules");
        return ResponseModel.success("Limit Rules", feeLimitRuleService.getAllLimitRules());
    }
    @GetMapping("/GetMerchantFeeRule/{merchantId}")
    public ResponseEntity<?> getFeeRuleByMerchant(@PathVariable String merchantId){
        log.info("Get Fee Rule for merchantId: {}, txnType: {}", merchantId);
        return ResponseModel.success("Fee Rule", feeLimitRuleService.getFeeRuleByMerchantId(merchantId));
        }
    @GetMapping("/GetMerchantlimitRule/{merchantId}")
    public ResponseEntity<?> getLimitRuleByMerchant(@PathVariable String merchantId){
        log.info("Get Fee Rule for merchantId: {}, txnType: {}", merchantId);
        return ResponseModel.success("Limit Rule", feeLimitRuleService.getLimitRuleByMerchantId(merchantId));
    }
    @GetMapping("/FeeRuleByMerchantAndTxnType")
    public ResponseEntity<?> getFeeRule(@RequestParam("merchantId") String merchantId, @RequestParam("txnType") String txnType){
        log.info("Get Fee Rule for merchantId: {}, txnType: {}", merchantId, txnType);
        return ResponseModel.success("Fee Rule", feeLimitRuleService.getFeeRuleByMerchantIdAndTxnType(merchantId, txnType));
    }
    @GetMapping("/LimitRuleByMerchantAndTxnType")
    public ResponseEntity<?> getLimitRule(@RequestParam("merchantId") String merchantId, @RequestParam("txnType") String txnType){
        log.info("Get Fee Rule for merchantId: {}, txnType: {}", merchantId, txnType);
        return ResponseModel.success("Limit Rule", feeLimitRuleService.getFeeRuleByMerchantIdAndTxnType(merchantId, txnType));
    }

     @PostMapping("/AddMerchantFeeRule")
     public ResponseEntity<?> addFeeRule(@RequestBody FeeRule feeRule){
         log.info("Add Fee Rule for merchantId: {}, txnType: {}, fee: {}", feeRule.getMerchantId(), feeRule.getTxnType(), feeRule.getFeeValue());
         feeLimitRuleService.saveFeeRule(feeRule);
         return ResponseModel.success("Fee Rule added successfully");
     }
    @PostMapping("/AddMerchantLimitRule")
    public ResponseEntity<?> addLimitRule(@RequestBody LimitRule limitRule){
        log.info("Add Fee Rule for merchantId: {}, txnType: {}", limitRule.getMerchantId(), limitRule.getTxnType());
        feeLimitRuleService.saveLimitRule(limitRule);
        return ResponseModel.success("Limit Rule added successfully");
    }
    @PostMapping("/UpdateMerchantFeeRule")
    public ResponseEntity<?> updateFeeRule(@RequestBody FeeRule feeRule){
        log.info("Update Fee Rule for merchantId: {}, txnType: {}, fee: {}", feeRule.getMerchantId(), feeRule.getTxnType(), feeRule.getFeeValue());
        feeLimitRuleService.updateFeeRule(feeRule);
        return ResponseModel.success("Fee Rule Updated successfully");
    }
    @PostMapping("/UpdateMerchantLimitRule")
    public ResponseEntity<?> updateLimitRule(@RequestBody LimitRule limitRule){
        log.info("Update Fee Rule for merchantId: {}, txnType: {}", limitRule.getMerchantId(), limitRule.getTxnType());
        feeLimitRuleService.updateLimitRule(limitRule);
        return ResponseModel.success("Limit Rule Updated successfully");
    }

    @PostMapping("/checkTxnFee")
    public ResponseEntity<?> checkTxnFee(@RequestBody TransactionFeeReq req){
        log.info("Check Fee Rule for merchantId: {}, txnType: {}", req.getMerchantId(), req.getTxnType());

         String feeType="PAYIN";//PAYIN,PAYOUT/TRANSFER
         BigDecimal txnAmount = new BigDecimal(100);

        BigDecimal fee = transactionService.processTransaction(req.getMerchantId(),req.getTxnType(),req.getTxnAmount());
        return ResponseModel.success("Fee & Limit Rule Checked successfully", Map.of(
                "txnAmount", req.getTxnAmount(),
                "calculatedFee", fee,
                "TxnType",req.getTxnType(),
                "effectiveAmount", req.getTxnAmount().add(fee)
        ));

    }

}
