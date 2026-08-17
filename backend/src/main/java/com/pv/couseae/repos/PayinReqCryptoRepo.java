package com.pv.couseae.repos;

import com.pv.couseae.entities.PayinRequestCrypto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayinReqCryptoRepo extends MongoRepository<PayinRequestCrypto,String> {
    Optional<PayinRequestCrypto> findByOrderId(String s);
    Optional<PayinRequestCrypto> findByCryptoOrderId(String s);
}
