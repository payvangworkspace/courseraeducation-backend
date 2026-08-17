package com.pv.couseae.repos;

import com.pv.couseae.entities.PayinStatusHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PayinStatusHistoryRepo extends MongoRepository<PayinStatusHistory, String> {
}
