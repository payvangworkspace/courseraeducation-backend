package com.pv.couseae.repos;

import com.pv.couseae.entities.Currency;
import com.pv.couseae.entities.LoadMoneyDetails;
import com.pv.couseae.entities.UserAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;

public interface LoadMoneyDetailsRepo extends MongoRepository<LoadMoneyDetails, String> {
Page<LoadMoneyDetails> findByUserAccount(UserAccount userAccount, Pageable pageable);

Page<LoadMoneyDetails> findByUserAccountAndCurrency(UserAccount userAccount, Currency currency, Pageable pageable);

Page<LoadMoneyDetails> findByUserAccountAndCurrencyAndCreatedDateBetween(UserAccount userAccount, Currency currency,
                                                                             LocalDateTime startDate, LocalDateTime endDate,
                                                                             Pageable pageable);

Page<LoadMoneyDetails> findByUserAccountAndCreatedDateBetween(UserAccount userAccount,
                                                                  LocalDateTime startDate, LocalDateTime endDate,
                                                                  Pageable pageable);

Page<LoadMoneyDetails> findByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);



}
