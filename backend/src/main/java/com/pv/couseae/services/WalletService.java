package com.pv.couseae.services;

import com.pv.couseae.entities.MerchantWallet;
import com.pv.couseae.repos.MerchantWalletRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final MongoTemplate mongoTemplate;
    private final MerchantWalletRepo walletRepo;

    public MerchantWallet getWallet(String merchantId) {
        return walletRepo.findById(merchantId).orElse(null);
    }
    public MerchantWallet saveWallet(MerchantWallet wallet) {
        wallet.setLastUpdated(LocalDateTime.now());

        return walletRepo.save(wallet);
    }
    public List<MerchantWallet> getAllWallets() {
        return walletRepo.findAll();
        }
    public MerchantWallet creditBalance(String merchantId, BigDecimal amount) {
        Query query = new Query(Criteria.where("_id").is(merchantId));
        Update update = new Update().inc("balance", amount.doubleValue()).currentDate("lastUpdated");

        return mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                MerchantWallet.class
        );
    }

    public MerchantWallet checkAndDeductBalance(String merchantId, BigDecimal amount) {
        Query query = new Query(
                Criteria.where("_id").is(merchantId)
                        .and("balance").gte(amount)
        );
        Update update = new Update().inc("balance", amount.negate()).currentDate("lastUpdated");

        MerchantWallet wallet = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                MerchantWallet.class
        );

        if (wallet == null) {
            throw new RuntimeException("Insufficient balance");
        }

        return wallet;
    }
}
