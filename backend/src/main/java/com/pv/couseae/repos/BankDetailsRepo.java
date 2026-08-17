package com.pv.couseae.repos;

import com.pv.couseae.entities.BankDetails;
import com.pv.couseae.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BankDetailsRepo extends MongoRepository<BankDetails, String> {

    BankDetails findByUserAndBankAccountNumberOrUserAndCardNumber(User user, String bankAccountNumber, User user1, String cardNumber);

    void deleteByBankDetailIdAndUser(String bankDetailId, User user);
}
