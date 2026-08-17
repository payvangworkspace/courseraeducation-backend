package com.pv.couseae.repos;

import com.pv.couseae.entities.MerchantCryptoWallet;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantCryptoWalletRepo extends MongoRepository<MerchantCryptoWallet, String> {

    Optional<MerchantCryptoWallet> findByMerchantIdAndCoin(String merchantId, String coin);
    List<MerchantCryptoWallet> findByMerchantId(String merchantId);
}
