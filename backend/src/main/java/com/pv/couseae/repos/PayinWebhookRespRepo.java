package com.pv.couseae.repos;

import com.pv.couseae.entities.PayinWebhookResp;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayinWebhookRespRepo extends MongoRepository<PayinWebhookResp,String> {
}
