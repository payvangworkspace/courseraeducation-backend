package com.pv.couseae.services;

import com.pv.couseae.entities.LocationCountry;
import com.pv.couseae.repos.LocationCityRepo;
import com.pv.couseae.repos.LocationCountryRepo;
import com.pv.couseae.repos.LocationStateRepo;
import com.pv.couseae.utill.SearchRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LocationServiceImpl implements LocationService {
    private LocationCountryRepo locationCountryRepo;
    private LocationStateRepo locationStateRepo;
    private LocationCityRepo locationCityRepo;

    @Override
    public Page<LocationCountry> getAllCountry(SearchRequest searchRequest) {
        Pageable pageable = PageRequest.of(searchRequest.getStart(), searchRequest.getSize());
        if (!searchRequest.getKeyword().isEmpty()){
            return this.locationCountryRepo.findAllByCountryNameLikeIgnoreCaseOrderByCountryName(searchRequest.getKeyword(),pageable);
        }
        return this.locationCountryRepo.findAllByOrderByCountryName(pageable);
    }

    @Override
    public LocationCountry getCountryByNameOrNumericCodeOrPhoneCode(String countryName, String countryNumericCode, String countryPhoneCode) {
        return this.locationCountryRepo.findAllByCountryNameOrCountryNumericCodeOrCountryPhoneCode(countryName, countryNumericCode, countryPhoneCode);
    }

    @Override
    public LocationCountry getCountryByNameOrCountryCode(String countryName, String countryCode) {
        return this.locationCountryRepo.findByCountryNameOrCountryCode(countryName,countryCode);
    }

    @Override
    public void addCountry(LocationCountry locationCountry) {
        this.locationCountryRepo.save(locationCountry);
    }

    @Override
    public LocationCountry getCountryById(String countryId) {
        return this.locationCountryRepo.findById(countryId).orElse(null);
    }

    @Override
    public void deleteCountry(String countryId) {
        this.locationCountryRepo.deleteById(countryId);
    }

}
