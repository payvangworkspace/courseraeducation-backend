package com.pv.couseae.repos;

import com.pv.couseae.entities.PaymentLinks;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;

public interface PaymentLinksRepo extends MongoRepository<PaymentLinks, Long> {
    Page<PaymentLinks> findAllByStatus(String keyword, Pageable pageable);

    Page<PaymentLinks> findAllByStatusLikeIgnoreCaseOrCountryCodeLikeIgnoreCaseOrCurrencyCodeLikeIgnoreCaseOrCustomerNameLikeIgnoreCaseOrCustomerEmailIdLikeIgnoreCaseOrCustomerContactNumberLikeIgnoreCase(String keyword, String keyword1, String keyword2, String keyword3, String keyword4, String keyword5, Pageable pageable);

    Page<PaymentLinks> findAllByMerchantIdAndStatus(String userName, String status, Pageable pageable);

    Page<PaymentLinks> findAllByMerchantId(String userName, Pageable pageable);


    Page<PaymentLinks> findAllByCurrencyCodeAndCreatedDateBetween(String currencyCode, Date from, Date to, Pageable pageable);
    Page<PaymentLinks> findAllByCurrencyCodeAndStatusAndCreatedDateBetween(String currencyCode, String status, Date from, Date to, Pageable pageable);
    Page<PaymentLinks> findAllByMerchantIdAndCurrencyCodeAndCreatedDateBetween(String merchantId, String currencyCode, Date from, Date to, Pageable pageable);
    Page<PaymentLinks> findAllByMerchantIdAndCurrencyCodeAndStatusAndCreatedDateBetween(String merchantId, String currencyCode, String status, Date from, Date to, Pageable pageable);


}
