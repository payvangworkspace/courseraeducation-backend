package com.pv.couseae.services;

import com.pv.couseae.entities.*;
import com.pv.couseae.enums.TransferType;
import com.pv.couseae.repos.CurrencyRepo;
import com.pv.couseae.repos.LoadMoneyDetailsRepo;
import com.pv.couseae.repos.UserAccountRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayinWalletService {
    private final UserAccountRepo userAccountRepo;
    private final UserService userService;
    private final CurrencyRepo currencyRepo;
    private final LoadMoneyDetailsRepo loadMoneyDetailsRepo;
    private final WalletService merchantWalletService;

    public LoadMoneyDetails addamountToWallet(String userId, double amount, String currencyId, String remark, String receiptId) {
        User existingUser = this.userService.userById(userId);

        Currency existingCurrency = this.currencyRepo.findById(currencyId).orElse(null);

        UserAccount existingAccount = this.userAccountRepo.findByUser(new User(userId));
        LoadMoneyDetails loadMoneyDetails = new LoadMoneyDetails();
        loadMoneyDetails.setCurrency(existingCurrency);
        loadMoneyDetails.setAmount(amount);
        loadMoneyDetails.setTransactionTypes(TransferType.CREDIT);
        MerchantWallet marchantwallet=merchantWalletService.getWallet(userId);
        if(marchantwallet!=null){
            merchantWalletService.creditBalance(userId, BigDecimal.valueOf(amount));
        }else{
            MerchantWallet wallet=new MerchantWallet();
            wallet.setMerchantId(userId);
            wallet.setBalance(BigDecimal.valueOf(amount));
            wallet.setCurrency(existingCurrency.getCurrencyCode());
            merchantWalletService.saveWallet(wallet);
        }
        if (existingAccount ==null){
            UserAccount newAccount = new UserAccount();
            newAccount.setUser(new User(userId));
            newAccount.setCurrency(new Currency(currencyId));
            newAccount.setAmountBalance(amount);
            UserAccount savedAccount = this.userAccountRepo.save(newAccount);
            loadMoneyDetails.setUserAccount(savedAccount);
            loadMoneyDetails.setPreviousBalance(0.0);
        }else {
            loadMoneyDetails.setUserAccount(existingAccount);
            loadMoneyDetails.setPreviousBalance(marchantwallet!=null?marchantwallet.getBalance().doubleValue():existingAccount.getAmountBalance());
            existingAccount.setAmountBalance(existingAccount.getAmountBalance()+amount);
            this.userAccountRepo.save(existingAccount);

        }
        loadMoneyDetails.setUpdatedBalance(loadMoneyDetails.getPreviousBalance()+amount);
        loadMoneyDetails.setRemark(remark);
        loadMoneyDetails.setReceiptId(receiptId);


        log.info("loadMoneyDetails: "+loadMoneyDetails);

        LoadMoneyDetails lsd= this.loadMoneyDetailsRepo.save(loadMoneyDetails);
        return lsd;
    }

}
