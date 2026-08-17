package com.pv.couseae.controller;

import com.pv.couseae.entities.LocationCountry;
import com.pv.couseae.entities.User;
import com.pv.couseae.model.CountryMapping;
import com.pv.couseae.services.LocationService;
import com.pv.couseae.services.UserService;
import com.pv.couseae.utill.ResponseModel;
import com.pv.couseae.utill.SearchRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Controller
//@CrossOrigin
@RequestMapping
@AllArgsConstructor
public class CountryController {

    private LocationService locationService;
    private UserService userService;

    // ----------- Country--------------------------
    @PostMapping("location/country")
    public ResponseEntity<?> newCountry(@RequestBody LocationCountry locationCountry) {
        LocationCountry isExisted = this.locationService.getCountryByNameOrNumericCodeOrPhoneCode(locationCountry.getCountryName(), locationCountry.getCountryNumericCode(), locationCountry.getCountryPhoneCode());
        if (isExisted != null && isExisted.getCountryName().equalsIgnoreCase(locationCountry.getCountryName()))
            return ResponseModel.customValidations("Country Name", "Country name is already existed");
        else if (isExisted != null && isExisted.getCountryNumericCode().equalsIgnoreCase(locationCountry.getCountryNumericCode()))
            return ResponseModel.customValidations("Country Numeric Code", "Country Numeric Code is already existed");
        else if (isExisted != null && isExisted.getCountryPhoneCode().equalsIgnoreCase(locationCountry.getCountryPhoneCode()))
            return ResponseModel.customValidations("Country Phone Code", "Country Phone Code is already existed");
        else {
            this.locationService.addCountry(locationCountry);
            return ResponseModel.created("Country added");
        }
    }

    @PostMapping("location/country/all")
    public ResponseEntity<?> getAllCountry(@RequestBody SearchRequest searchRequest) {
        Page<LocationCountry> allCountry = this.locationService.getAllCountry(searchRequest);
        return ResponseModel.success("All country list", allCountry);
    }

    @PostMapping("location/country/update")
    public ResponseEntity<?> updateCountry(@RequestBody LocationCountry locationCountry) {
        if (locationCountry.getCountryId() == null || locationCountry.getCountryId().isEmpty())
            return ResponseModel.error("Country Id not found");
        if (locationCountry.getCountryCapital().isEmpty())
            return ResponseModel.error("Country Capital Name not found");
        if (locationCountry.getCountryNumericCode().isEmpty())
            return ResponseModel.error("Country Numeric Code not found");
        if (locationCountry.getCountryPhoneCode().isEmpty())
            return ResponseModel.error("Country Numeric Code not found");
        this.locationService.addCountry(locationCountry);
        return ResponseModel.success("Country updated");
    }
    @DeleteMapping("location/country/{countryId}")
    public ResponseEntity<?> deleteCountry(@PathVariable String countryId){
        try {
            this.locationService.deleteCountry(countryId);
            return ResponseModel.deleted();
        } catch (Exception e) {
           return ResponseModel.error("Country already assign to user");
        }
    }
    // ----------- End Country--------------------------

    // -----------------------Country Mapping----------------------------------------------------
    @PostMapping("country/mapping")
    ResponseEntity<?> mapCountry(@RequestBody CountryMapping countryMapping) {
        User user = this.userService.userById(countryMapping.getUserId());
        Set<LocationCountry> countryList = new HashSet<>();
        if (user.getCountries() != null) {
            for (LocationCountry item : user.getCountries()) {
                countryMapping.getCountries().add(item.getCountryId());
            }
        }
        for (String ids : countryMapping.getCountries()) {
            countryList.add(new LocationCountry(ids));
        }
        user.setCountries(countryList);
        this.userService.updateUser(user);
        return ResponseModel.created("Country updated");
    }

    @GetMapping("country/mapping/{userId}")
    ResponseEntity<?> allCountry(@PathVariable String userId) {
        User user = this.userService.userById(userId);
        if(user!=null){
            log.info("User found with id: " + userId+" and contries: "+user.getCountries());
        }
        return ResponseModel.success("All country list of user", user.getCountries());
    }

    @DeleteMapping("country/mapping/{userId}/{countryId}")
    ResponseEntity<?> removeCurrency(@PathVariable String userId, @PathVariable String countryId) {
        User user = this.userService.userById(userId);
        Set<String> countries = new HashSet<>();
        for (LocationCountry items : user.getCountries()) {
            if (!items.getCountryId().equalsIgnoreCase(countryId)) {
                countries.add(items.getCountryId());
            }
        }
        Set<LocationCountry> countryList = new HashSet<>();
        for (String ids : countries) {
            countryList.add(new LocationCountry(ids));
        }
        user.setCountries(countryList);
        this.userService.updateUser(user);
        return ResponseModel.deleted();
    }
}
