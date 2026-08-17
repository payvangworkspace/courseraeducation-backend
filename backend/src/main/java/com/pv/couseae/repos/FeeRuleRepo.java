package com.pv.couseae.repos;

import com.pv.couseae.entities.FeeRule;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FeeRuleRepo extends MongoRepository<FeeRule, String> {
//    merchantId,txnType
    List<FeeRule> findByMerchantId(String merchantId);
    List<FeeRule> findByMerchantIdAndTxnType(String merchantId, String txnType);
}
