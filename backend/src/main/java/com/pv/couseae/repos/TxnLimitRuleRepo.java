package com.pv.couseae.repos;

import com.pv.couseae.entities.LimitRule;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TxnLimitRuleRepo extends MongoRepository<LimitRule, String> {
//    merchantId,txnType
    List<LimitRule> findByMerchantId(String merchantId);
    List<LimitRule> findByMerchantIdAndTxnType(String merchantId, String txnType);
}
