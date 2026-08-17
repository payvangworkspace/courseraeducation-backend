package com.pv.couseae.repos;

import com.pv.couseae.entities.LocationCountry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LocationCountryRepo extends MongoRepository<LocationCountry, String> {
    LocationCountry findAllByCountryNameOrCountryNumericCodeOrCountryPhoneCode(String countryName, String countryNumericCode, String countryPhoneCode);

    Page<LocationCountry> findAllByCountryNameLikeIgnoreCaseOrderByCountryName(String keyword, Pageable pageable);

    Page<LocationCountry> findAllByOrderByCountryName(Pageable pageable);

    LocationCountry findByCountryNameOrCountryCode(String countryName, String countryCode);
}
