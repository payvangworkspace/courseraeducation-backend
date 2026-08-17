package com.pv.couseae.services;

import com.pv.couseae.entities.Currency;
import com.pv.couseae.repos.CurrencyRepo;
import com.pv.couseae.utill.SearchRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CurrencyServicesImpl implements CurrencyServices {
    private CurrencyRepo currencyRepo;

    @Override
    public Currency getByNameOrCode(String currencyName, String currencyCode) {
        return this.currencyRepo.findAllByCurrencyNameOrCurrencyCode(currencyName, currencyCode);
    }

    @Override
    public void addCurrency(Currency currency) {
        this.currencyRepo.save(currency);
    }

    @Override
    public Page<Currency> getAll(SearchRequest searchRequest) {
        Pageable pageable = PageRequest.of(searchRequest.getStart(), searchRequest.getSize());
        if (!searchRequest.getKeyword().isEmpty()){
            return this.currencyRepo.findAllByCurrencyNameLikeIgnoreCaseOrderByCurrencyName(searchRequest.getKeyword(),pageable);
        }
        return this.currencyRepo.findAllByOrderByCurrencyName(pageable);
    }

    @Override
    public void updateCurrency(Currency currency) {
        this.currencyRepo.save(currency);
    }

    @Override
    public Currency getById(String currencyId) {
        return this.currencyRepo.findById(currencyId).orElse(null);
    }

    @Override
    public void saveAll(List<Currency> list) {
        this.currencyRepo.saveAll(list);
    }

    @Override
    public void deleteCurrency(String currencyId) {
        this.currencyRepo.deleteById(currencyId);
    }

    @Override
    public Currency findByCurrencyCode(String currencyCode) {
        return this.currencyRepo.findByCurrencyCode(currencyCode);
    }
}
