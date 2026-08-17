package com.pv.couseae.repos;

import com.pv.couseae.entities.LocationCountry;
import com.pv.couseae.entities.LocationState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LocationStateRepo extends MongoRepository<LocationState, String> {
    Page<LocationState> findAllByCountry(LocationCountry locationCountry, Pageable pageable);

    LocationState findAllByStateNameOrStateCode(String stateName, String stateCode);
}
