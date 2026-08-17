package com.pv.couseae.repos;

import com.pv.couseae.entities.MerchantWallet;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantWalletRepo extends MongoRepository<MerchantWallet, String> {
    // Basic CRUD methods are available by default
}