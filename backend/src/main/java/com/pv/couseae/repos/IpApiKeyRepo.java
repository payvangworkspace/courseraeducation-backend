package com.pv.couseae.repos;

import com.pv.couseae.entities.IpApiKeyInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IpApiKeyRepo extends MongoRepository<IpApiKeyInfo, String> {
    Optional<IpApiKeyInfo> findByAllowedIps(String ip);
    Optional<IpApiKeyInfo> findByAllowedIpsAndMerchantId(String ip,String merchantId);
    Optional<IpApiKeyInfo> findByKeyHash(String keyhash);
     IpApiKeyInfo findByMerchantId(String merchantId);
}
