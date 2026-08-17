package com.pv.couseae.repos;

import com.pv.couseae.entities.AddressDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AddressDetailsRepo extends MongoRepository<AddressDetails, String> {

}
