package com.pv.couseae.repos;

import com.pv.couseae.entities.LocationCity;
import com.pv.couseae.entities.LocationState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LocationCityRepo extends MongoRepository<LocationCity, String> {
    Page<LocationCity> findAllByState(LocationState locationState, Pageable pageable);

    LocationCity findAllByCityNameOrCityCode(String cityName, String cityCode);
}
