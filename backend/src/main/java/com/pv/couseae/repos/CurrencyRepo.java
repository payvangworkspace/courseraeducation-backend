package com.pv.couseae.repos;

import com.pv.couseae.entities.Currency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CurrencyRepo extends MongoRepository<Currency, String> {

    Currency findAllByCurrencyNameOrCurrencyCode(String currencyName, String currencyCode);

    Page<Currency> findAllByCurrencyNameLikeIgnoreCaseOrderByCurrencyName(String keyword, Pageable pageable);

    Page<Currency> findAllByOrderByCurrencyName(Pageable pageable);

    Currency findByCurrencyCode(String currencyCode);
}
