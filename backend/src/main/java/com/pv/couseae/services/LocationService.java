package com.pv.couseae.services;

import com.pv.couseae.entities.LocationCountry;
import com.pv.couseae.utill.SearchRequest;
import org.springframework.data.domain.Page;

public interface LocationService {
    Page<LocationCountry> getAllCountry(SearchRequest searchRequest);

    LocationCountry getCountryByNameOrNumericCodeOrPhoneCode(String countryName, String countryNumericCode, String countryPhoneCode);
    LocationCountry getCountryByNameOrCountryCode(String countryName, String countryCode);

    void addCountry(LocationCountry locationCountry);

    LocationCountry getCountryById(String countryId);

    void deleteCountry(String countryId);
}
