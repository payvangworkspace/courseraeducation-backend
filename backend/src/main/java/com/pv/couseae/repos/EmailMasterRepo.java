package com.pv.couseae.repos;


import com.pv.couseae.entities.EmailMaster;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailMasterRepo extends MongoRepository<EmailMaster, String> {
    EmailMaster findByEmailCode(String emailCode);
}
