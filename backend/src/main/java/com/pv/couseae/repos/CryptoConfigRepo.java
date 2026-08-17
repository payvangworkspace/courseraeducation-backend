package com.pv.couseae.repos;

import com.pv.couseae.entities.MerchantCryptoConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CryptoConfigRepo extends MongoRepository<MerchantCryptoConfig,String>  {
    List<MerchantCryptoConfig> findByMerchantId(String MerchantId);
    List<MerchantCryptoConfig> findByMerchantIdAndStatusTrue(String MerchantId);

}
