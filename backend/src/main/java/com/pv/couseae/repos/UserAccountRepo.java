package com.pv.couseae.repos;

import com.pv.couseae.entities.Currency;
import com.pv.couseae.entities.User;
import com.pv.couseae.entities.UserAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserAccountRepo extends MongoRepository<UserAccount, String> {
    UserAccount findByUserAndCurrency(User user, Currency currency);
    UserAccount findByUser(User user);

    Page<UserAccount> findAllByUserAndCurrencyOrderByUser(User user, Currency currency, Pageable pageable);

    Page<UserAccount> findAllByUserOrderByUser(User user, Pageable pageable);

    Page<UserAccount> findAllByCurrencyOrderByUser(Currency currency, Pageable pageable);
}
