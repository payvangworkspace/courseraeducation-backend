package com.pv.couseae.repos;

import com.pv.couseae.entities.BusinessDetails;
import com.pv.couseae.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BusinessDetailsRepo extends MongoRepository<BusinessDetails, String> {
    BusinessDetails findByUser(User user);
}
