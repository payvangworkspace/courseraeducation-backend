package com.pv.couseae.services;

import com.mongodb.client.result.UpdateResult;
import com.pv.couseae.entities.MerchantCryptoWallet;
import com.pv.couseae.handlers.InsufficientBalanceException;
import com.pv.couseae.repos.MerchantCryptoWalletRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CryptoWalletService {
    private final MerchantCryptoWalletRepo cryptoRepo;
    private final MongoTemplate mongoTemplate;

    public List<MerchantCryptoWallet> getCryptoWallet(String merchantId) {
        return cryptoRepo.findByMerchantId(merchantId);
    }
    public List<MerchantCryptoWallet> getAllCryptoWallets() {
        return cryptoRepo.findAll();
    }

    public MerchantCryptoWallet SaveCryptoWallet(String merchantId,String coin,BigDecimal amount){
        MerchantCryptoWallet wallet = cryptoRepo.findByMerchantIdAndCoin(merchantId, coin) .orElse( MerchantCryptoWallet.builder()
                                        .merchantId(merchantId)
                                        .coin(coin)
                                        .balance(BigDecimal.ZERO)
                                        .build());

        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setLastUpdated(LocalDateTime.now());

        cryptoRepo.save(wallet);
         return  wallet;
    }
    public void updateCryptoWallet(String merchantId, String coin, String network, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Amount must not be null or zero");
        }

        String normalizedCoin = coin.trim().toUpperCase();
        String normalizedNetwork = network.trim().toUpperCase();

        Query query = Query.query( Criteria.where("merchantId").is(merchantId)
                .and("coin").is(normalizedCoin)
                .and("network").is(normalizedNetwork) );

        Update update = new Update()
                .inc("balance", amount.setScale(8, RoundingMode.HALF_UP))
                .setOnInsert("merchantId", merchantId)
                .setOnInsert("coin", normalizedCoin)
                .setOnInsert("network", normalizedNetwork)   // 🔥 IMPORTANT
                .setOnInsert("balance", BigDecimal.ZERO)     // ensures clean insert
                .setOnInsert("createdAt", LocalDateTime.now())
                .set("lastUpdated", LocalDateTime.now());

        mongoTemplate.upsert(query, update, MerchantCryptoWallet.class);
    }
    public void debitCryptoWallet(String merchantId, String coin, BigDecimal amount ) {
        Query query = Query.query( Criteria.where("merchantId").is(merchantId)
                        .and("coin").is(coin)
                        .and("balance").gte(amount)  );

        Update update = new Update().inc("balance", amount.negate()).set("lastUpdated", LocalDateTime.now());

        UpdateResult result =mongoTemplate.updateFirst(query, update, MerchantCryptoWallet.class);

        if (result.getModifiedCount() == 0) {
            throw new InsufficientBalanceException("Insufficent Balance...");
        }
    }
    public void debitCryptoWallet(String merchantId, String coin, String network,BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid debit amount");
        }

        Query query = Query.query( Criteria.where("merchantId").is(merchantId).and("coin").is(coin.toUpperCase())
                        .and("network").is(network.toUpperCase())
                        .and("balance").gte(amount) );

        Update update = new Update().inc("balance", amount.negate()).set("lastUpdated", LocalDateTime.now());

        UpdateResult result = mongoTemplate.updateFirst(query, update, MerchantCryptoWallet.class);

        if (result.getModifiedCount() == 0) {
            throw new InsufficientBalanceException( "Insufficient balance for " + coin + " on " + network );
        }
    }
    public MerchantCryptoWallet checkdebitCryptoWallet(String merchantId, String coin, BigDecimal amount) {

        Query query = new Query(Criteria.where("merchantId").is(merchantId).and("coin").is(coin).and("balance").gte(amount));

        Update update = new Update().inc("balance", amount.negate()).currentDate("lastUpdated");

        MerchantCryptoWallet wallet = mongoTemplate.findAndModify(query,update,FindAndModifyOptions.options().returnNew(true),MerchantCryptoWallet.class);

        if (wallet == null) {
            throw new InsufficientBalanceException("Insufficient Balance...");
        }

        return wallet;
    }
}
