package com.pv.couseae.services;

import com.pv.couseae.entities.Currency;
import com.pv.couseae.utill.SearchRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CurrencyServices {
    Currency getByNameOrCode(String currencyName, String currencyCode);

    void addCurrency(Currency currency);

    Page<Currency> getAll(SearchRequest searchRequest);

    void updateCurrency(Currency currency);

    Currency getById(String currencyId);

    void saveAll(List<Currency> list);

    void deleteCurrency(String currencyId);

    Currency findByCurrencyCode(String currencyCode);
}
