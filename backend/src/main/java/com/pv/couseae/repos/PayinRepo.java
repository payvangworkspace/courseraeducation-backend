package com.pv.couseae.repos;

import com.pv.couseae.entities.PayinRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayinRepo extends MongoRepository<PayinRequest, String> {

    List<PayinRequest> findByTransactionStatusAndSettledFalseAndChargesIsNull(String transactionStatus, Pageable pageable);
    List<PayinRequest> findByTransactionStatusAndSettledFalse(String transactionStatus, Pageable pageable);
}
