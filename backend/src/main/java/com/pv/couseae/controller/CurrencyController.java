package com.pv.couseae.controller;

import com.pv.couseae.entities.Currency;
import com.pv.couseae.entities.User;
import com.pv.couseae.model.CurrencyMapping;
import com.pv.couseae.services.CurrencyServices;
import com.pv.couseae.services.UserService;
import com.pv.couseae.utill.ResponseModel;
import com.pv.couseae.utill.SearchRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@RestController
//@CrossOrigin
@RequestMapping("currency")
@AllArgsConstructor
public class CurrencyController {
    private CurrencyServices currencyServices;
    private ModelMapper mapper;
    private UserService userService;

    @PostMapping
    @SneakyThrows
    ResponseEntity<?> addCurrency(@RequestBody Currency currency) {

        Currency isExisting = this.currencyServices.getByNameOrCode(currency.getCurrencyName(), currency.getCurrencyCode());
        if (isExisting != null && isExisting.getCurrencyName().equalsIgnoreCase(currency.getCurrencyName()))
            return ResponseModel.error("Currency Name already existed");
        else if (isExisting != null && isExisting.getCurrencyCode().equalsIgnoreCase(currency.getCurrencyCode()))
            return ResponseModel.error("Currency code already existed");
        this.currencyServices.addCurrency(currency);
        return ResponseModel.success("Currency added successfully");
    }

    @PutMapping
    ResponseEntity<?> updateCurrency(@Valid @RequestBody Currency currency) {
        if (currency.getCurrencyId().isEmpty()) {
            return ResponseModel.error("Currency id is not found");
        }
        this.currencyServices.updateCurrency(currency);
        return ResponseModel.success("Currency updated successfully");
    }

    @PostMapping("all")
    ResponseEntity<?> getAll(@RequestBody SearchRequest searchRequest) {
        Page<Currency> currencies = this.currencyServices.getAll(searchRequest);
        return ResponseModel.success("All Currency", currencies);
    }

    @DeleteMapping("{currencyId}")
    public ResponseEntity<?> deleteCurrency(@PathVariable String currencyId){
        try {
            this.currencyServices.deleteCurrency(currencyId);
            return ResponseModel.deleted();
        } catch (Exception e) {
            return ResponseModel.error("Currency already assign to user");
        }
    }

    // -----------------------Currency Mapping----------------------------------------------------
    @PostMapping("mapping")
    ResponseEntity<?> addCurrency(@RequestBody CurrencyMapping currencyMapping) {
        User user = this.userService.userById(currencyMapping.getUserId());
        Set<Currency> currencyList = new HashSet<>();
        if (user.getCurrencies() != null){
            for (Currency items: user.getCurrencies()){
                currencyMapping.getCurrencies().add(items.getCurrencyId());
            }
        }
        for (String ids : currencyMapping.getCurrencies()) {
            currencyList.add(new Currency(ids));
        }
        user.setCurrencies(currencyList);
        this.userService.updateUser(user);
        return ResponseModel.created("currency added");
    }

    @GetMapping("mapping/{merchantId}")
    ResponseEntity<?> allCurrency(@PathVariable String merchantId) {
        User user = this.userService.userById(merchantId);
        return ResponseModel.success("All currency list of user", user.getCurrencies());
    }

    @DeleteMapping("mapping/{merchantId}/{currencyId}")
    ResponseEntity<?> removeCurrency(@PathVariable String merchantId, @PathVariable String currencyId) {
        User user = this.userService.userById(merchantId);
        Set<String> currencies = new HashSet<>();
        for (Currency items : user.getCurrencies()) {
            if (!items.getCurrencyId().equalsIgnoreCase(currencyId))
                currencies.add(items.getCurrencyId());
        }
        Set<Currency> currenciesList = new HashSet<>();
        for (String ids : currencies) {
            currenciesList.add(new Currency(ids));
        }
        user.setCurrencies(currenciesList);
        this.userService.updateUser(user);
        return ResponseModel.deleted();
    }
    // -----------------------End Currency Mapping----------------------------------------------------
}
