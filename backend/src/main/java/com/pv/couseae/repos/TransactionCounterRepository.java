package com.pv.couseae.repos;

import com.pv.couseae.entities.TransactionCounter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionCounterRepository extends MongoRepository<TransactionCounter, String> {
}