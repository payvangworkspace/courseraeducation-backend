package com.pv.couseae.repos;

import com.pv.couseae.entities.CryptoWebhookResp;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CryptoWebhookRespRepo extends MongoRepository<CryptoWebhookResp,String> {
}
