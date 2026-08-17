package com.pv.couseae.services;


import com.pv.couseae.entities.FeeRule;
import com.pv.couseae.entities.LimitRule;
import com.pv.couseae.repos.FeeRuleRepo;
import com.pv.couseae.repos.TxnLimitRuleRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeeAndLimitRuleService {

    private FeeRuleRepo feeRuleRepo;
    private TxnLimitRuleRepo txnLimitRuleRepo;

    public FeeAndLimitRuleService(FeeRuleRepo feeRuleRepo, TxnLimitRuleRepo txnLimitRuleRepo) {
        this.feeRuleRepo = feeRuleRepo;
        this.txnLimitRuleRepo = txnLimitRuleRepo;
    }
    public void saveFeeRule(FeeRule feeRule) {
        feeRuleRepo.save(feeRule);
    }
    public void updateFeeRule(FeeRule feeRule) {
        feeRuleRepo.save(feeRule);
    }
    public List<FeeRule> getFeeRuleByMerchantIdAndTxnType(String merchantId, String txnType) {
        return feeRuleRepo.findByMerchantIdAndTxnType(merchantId, txnType);
    }
    public List<FeeRule> getFeeRuleByMerchantId(String merchantId) {
        return feeRuleRepo.findByMerchantId(merchantId);
    }
    public List<FeeRule> getAllFeeRules() {
        return feeRuleRepo.findAll();
    }
    public void deleteFeeRule(FeeRule feeRule) {
        feeRuleRepo.delete(feeRule);
    }
    public void saveLimitRule(LimitRule txnLimitRule) {
        txnLimitRuleRepo.save(txnLimitRule);
    }
    public void updateLimitRule(LimitRule txnLimitRule) {
        txnLimitRuleRepo.save(txnLimitRule);
    }
    public List<LimitRule> getLimitRuleByMerchantId(String merchantId) {
        return txnLimitRuleRepo.findByMerchantId(merchantId);
    }
    public List<LimitRule> getLimitRuleByMerchantIdAndTxnType(String merchantId, String txnType) {
        return txnLimitRuleRepo.findByMerchantIdAndTxnType(merchantId, txnType);
    }
    public List<LimitRule> getAllLimitRules() {
        return txnLimitRuleRepo.findAll();
    }
    public void deleteLimitRule(LimitRule txnLimitRule) {
        txnLimitRuleRepo.delete(txnLimitRule);
    }


}
