package com.pv.couseae.services;

import com.pv.couseae.entities.FeeRule;
import com.pv.couseae.entities.LimitRule;
import com.pv.couseae.entities.TransactionCounter;
import com.pv.couseae.handlers.TransactionLimitException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class TransactionService {

    private final MongoTemplate mongoTemplate;
    private final FeeAndLimitRuleService feeLimitRuleService;

    public TransactionService(MongoTemplate mongoTemplate, FeeAndLimitRuleService feeLimitRuleService) {
        this.mongoTemplate = mongoTemplate;
        this.feeLimitRuleService = feeLimitRuleService;
    }

    public BigDecimal processTransaction(String merchantId, String txnType, BigDecimal txnAmount) {


        // 1️⃣ Fetch rules
        FeeRule feeRule = feeLimitRuleService.getFeeRuleByMerchantIdAndTxnType(merchantId, txnType)
                .stream().filter(rule -> Boolean.TRUE.equals(rule.isActive()))   // ✅ check active only
                .findFirst() // findFirst()
                .orElseThrow(() -> new TransactionLimitException("No fee rule configured"));

        LimitRule limitRule = feeLimitRuleService.getLimitRuleByMerchantIdAndTxnType(merchantId, txnType)
                .stream()
                .filter(rule -> Boolean.TRUE.equals(rule.isActive()))
                .findFirst()
                .orElseThrow(() -> new TransactionLimitException("No limit rule configured"));

        BigDecimal perTxnMin   = toBigDecimal(limitRule.getPerTxnMin());
        BigDecimal perTxnMax   = toBigDecimal(limitRule.getPerTxnMax());
        BigDecimal dailyLimit  = toBigDecimal(limitRule.getDailyLimit());
        BigDecimal monthlyLimit= toBigDecimal(limitRule.getMonthlyLimit());

        BigDecimal feeValue = feeRule.getFeeValue();

        String feeType      = feeRule.getFeeType(); // FLAT, PERCENT, MIXED
        BigDecimal capMin   = feeRule.getCapMin();
        BigDecimal capMax   = feeRule.getCapMax();
        BigDecimal percentage = feeRule.getCommissionPercent();
        log.info("Fee type: {}, Fee value: {}, Cap min: {}, Cap max: {}", feeType, feeValue, capMin, capMax);
//log.info("Fee rule: {}, Limit rule: {}", feeRule, limitRule);
        log.info("Per-txn min: {}, Per-txn max: {}, Daily limit: {}, Monthly limit: {}", perTxnMin, perTxnMax, dailyLimit, monthlyLimit);
        // 2️⃣ Per-transaction limit check
        if (txnAmount.compareTo(perTxnMin) < 0 || txnAmount.compareTo(perTxnMax) > 0) {
            throw new TransactionLimitException("Transaction amount violates per-transaction limit");
        }

        // 3️⃣ Daily & monthly counters
        TransactionCounter counter = mongoTemplate.findOne(Query.query(Criteria.where("merchantId").is(merchantId).and("txnType").is(txnType)),
                TransactionCounter.class);

        LocalDate today = LocalDate.now();
        String dayStr = today.format(DateTimeFormatter.ISO_DATE);
        String monthStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        Update update = new Update();
        boolean needsUpdate = false;

        if (counter == null) {
            // First transaction → create new document
            counter = TransactionCounter.builder()
                    .merchantId(merchantId)
                    .txnType(txnType)
                    .daily(new TransactionCounter.DailyCounter(dayStr, txnAmount))
                    .monthly(new TransactionCounter.MonthlyCounter(monthStr, txnAmount))
                    .build();
            mongoTemplate.save(counter);
        } else {
            // Reset daily if day changed
            if (!dayStr.equals(counter.getDaily().getDate())) {
                update.set("daily.amount", txnAmount);
                update.set("daily.date", dayStr);
                needsUpdate = true;
            } else {
                update.inc("daily.amount", txnAmount);
                needsUpdate = true;
            }

            // Reset monthly if month changed
            if (!monthStr.equals(counter.getMonthly().getMonth())) {
                update.set("monthly.amount", txnAmount);
                update.set("monthly.month", monthStr);
                needsUpdate = true;
            } else {
                update.inc("monthly.amount", txnAmount);
                needsUpdate = true;
            }

            if (needsUpdate) {
                FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);
                counter = mongoTemplate.findAndModify(Query.query(Criteria.where("merchantId").is(merchantId)
                        .and("txnType").is(txnType)), update,options,TransactionCounter.class);
            }
        }

// Validate limits
        if (counter.getDaily().getAmount().compareTo(dailyLimit) > 0) {
            throw new RuntimeException("Daily transaction limit exceeded");
        }
        if (counter.getMonthly().getAmount().compareTo(monthlyLimit) > 0) {
            throw new RuntimeException("Monthly transaction limit exceeded");
        }

        // 4️⃣ Fee calculation
        log.info("Fee type: {}, Fee value: {}, Cap min: {}, Cap max: {}", feeType, feeValue, capMin, capMax);
        BigDecimal fee = switch (feeType) {
            case "FLAT"    -> feeValue;
            case "PERCENT" -> txnAmount.multiply(percentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case "MIXED"   -> txnAmount.multiply(feeValue)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    .add(feeValue); // flat + percent example
            default        -> BigDecimal.ZERO;
        };

        if (capMin != null) fee = fee.max(capMin);
        if (capMax != null) fee = fee.min(capMax);

        return fee;
    }
    public Boolean payinTxnLimit(String merchantId, String txnType, BigDecimal txnAmount) {

        LimitRule limitRule = feeLimitRuleService.getLimitRuleByMerchantIdAndTxnType(merchantId, txnType)
                .stream()
                .filter(rule -> Boolean.TRUE.equals(rule.isActive()))
                .findFirst()
                .orElseThrow(() -> new TransactionLimitException("No limit rule configured"));

        BigDecimal perTxnMin   = toBigDecimal(limitRule.getPerTxnMin());
        BigDecimal perTxnMax   = toBigDecimal(limitRule.getPerTxnMax());
        BigDecimal dailyLimit  = toBigDecimal(limitRule.getDailyLimit());
        BigDecimal monthlyLimit= toBigDecimal(limitRule.getMonthlyLimit());

        log.info("Per-txn min: {}, Per-txn max: {}, Daily limit: {}, Monthly limit: {}", perTxnMin, perTxnMax, dailyLimit, monthlyLimit);
        // 2️⃣ Per-transaction limit check
        if (txnAmount.compareTo(perTxnMin) < 0 || txnAmount.compareTo(perTxnMax) > 0) {
            throw new TransactionLimitException("Transaction amount violates per-transaction limit");
        }

        // 3️⃣ Daily & monthly counters
        TransactionCounter counter = mongoTemplate.findOne(Query.query(Criteria.where("merchantId").is(merchantId).and("txnType").is(txnType)),
                TransactionCounter.class);

        LocalDate today = LocalDate.now();
        String dayStr = today.format(DateTimeFormatter.ISO_DATE);
        String monthStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        Update update = new Update();
        boolean needsUpdate = false;

        if (counter == null) {
            // First transaction → create new document
            counter = TransactionCounter.builder()
                    .merchantId(merchantId)
                    .txnType(txnType)
                    .daily(new TransactionCounter.DailyCounter(dayStr, txnAmount))
                    .monthly(new TransactionCounter.MonthlyCounter(monthStr, txnAmount))
                    .build();
            mongoTemplate.save(counter);
        } else {
            // Reset daily if day changed
            if (!dayStr.equals(counter.getDaily().getDate())) {
                update.set("daily.amount", txnAmount);
                update.set("daily.date", dayStr);
                needsUpdate = true;
            } else {
                update.inc("daily.amount", txnAmount);
                needsUpdate = true;
            }

            // Reset monthly if month changed
            if (!monthStr.equals(counter.getMonthly().getMonth())) {
                update.set("monthly.amount", txnAmount);
                update.set("monthly.month", monthStr);
                needsUpdate = true;
            } else {
                update.inc("monthly.amount", txnAmount);
                needsUpdate = true;
            }

            if (needsUpdate) {
                FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);
                counter = mongoTemplate.findAndModify(Query.query(Criteria.where("merchantId").is(merchantId)
                        .and("txnType").is(txnType)), update,options,TransactionCounter.class);
            }
        }

// Validate limits
        if (counter.getDaily().getAmount().compareTo(dailyLimit) > 0) {
            throw new TransactionLimitException("Daily transaction limit exceeded");
        }
        if (counter.getMonthly().getAmount().compareTo(monthlyLimit) > 0) {
            throw new TransactionLimitException("Monthly transaction limit exceeded");
        }

        return true;
    }
    public BigDecimal payinTransactionFee(String merchantId, String txnType, BigDecimal txnAmount) {


        // 1️⃣ Fetch rules
        FeeRule feeRule = feeLimitRuleService.getFeeRuleByMerchantIdAndTxnType(merchantId, txnType)
                .stream().filter(rule -> Boolean.TRUE.equals(rule.isActive()))   // ✅ check active only
                .findFirst() // findFirst()
                .orElseThrow(() -> new RuntimeException("No fee rule configured"));



        BigDecimal feeValue = feeRule.getFeeValue();

        String feeType      = feeRule.getFeeType(); // FLAT, PERCENT, MIXED
        BigDecimal capMin   = feeRule.getCapMin();
        BigDecimal capMax   = feeRule.getCapMax();
        BigDecimal percentage = feeRule.getCommissionPercent();

        // 4️⃣ Fee calculation
        log.info("Fee type: {}, Fee value: {}, Cap min: {}, Cap max: {}", feeType, feeValue, capMin, capMax);
        BigDecimal fee = switch (feeType) {
            case "FLAT"    -> feeValue;
            case "PERCENT" -> txnAmount.multiply(percentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case "MIXED"   -> txnAmount.multiply(feeValue)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    .add(feeValue); // flat + percent example
            default        -> BigDecimal.ZERO;
        };

        if (capMin != null) fee = fee.max(capMin);
        if (capMax != null) fee = fee.min(capMax);

        return fee;
    }
    public BigDecimal processTransactionFeeOnly(String merchantId, String txnType, BigDecimal txnAmount) {


        // 1️⃣ Fetch rules
        FeeRule feeRule = feeLimitRuleService.getFeeRuleByMerchantIdAndTxnType(merchantId, txnType)
                .stream().filter(rule -> Boolean.TRUE.equals(rule.isActive()))   // ✅ check active only
                .findFirst() // findFirst()
                .orElseThrow(() -> new TransactionLimitException("No fee rule configured"));



        BigDecimal feeValue = feeRule.getFeeValue();

        String feeType      = feeRule.getFeeType(); // FLAT, PERCENT, MIXED
        BigDecimal capMin   = feeRule.getCapMin();
        BigDecimal capMax   = feeRule.getCapMax();
        BigDecimal percentage = feeRule.getCommissionPercent();

        // 4️⃣ Fee calculation
        // log.info("Fee type: {}, Fee value: {},Fee % :{}, Cap min: {}, Cap max: {}", feeType, feeValue, percentage, capMin, capMax);
        BigDecimal fee = switch (feeType) {
            case "FLAT"    -> feeValue;
            case "PERCENT" -> txnAmount.multiply(percentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case "MIXED"   -> txnAmount.multiply(feeValue)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    .add(feeValue); // flat + percent example
            default        -> BigDecimal.ZERO;
        };

        if (capMin != null) fee = fee.max(capMin);
        if (capMax != null) fee = fee.min(capMax);

        return fee;
    }
    public BigDecimal toBigDecimal(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format: " + value, e);
        }
    }
}