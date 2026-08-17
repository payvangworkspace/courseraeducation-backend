package com.pv.couseae.repos;

import com.pv.couseae.entities.MerchantCryptoKeys;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantCryptoKeysRepo extends MongoRepository<MerchantCryptoKeys,String> {

    Optional<MerchantCryptoKeys> findByMerchantId(String merchantId);
}
