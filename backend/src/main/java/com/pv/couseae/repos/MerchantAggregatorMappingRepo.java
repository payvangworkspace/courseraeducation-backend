package com.pv.couseae.repos;

import com.pv.couseae.entities.MerchantAggregatorMapping;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantAggregatorMappingRepo extends MongoRepository<MerchantAggregatorMapping, String> {

    List<MerchantAggregatorMapping> findByMerchantIdAndEnvironment(String merchantId, String environment);
    List<MerchantAggregatorMapping> findByMerchantId(String merchantId);

}
