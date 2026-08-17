package com.pv.couseae.services;

import com.pv.couseae.Dtos.ApiRoutingContext;
import com.pv.couseae.entities.ApiMaster;
import com.pv.couseae.entities.MerchantAggregatorMapping;
import com.pv.couseae.entities.User;
import com.pv.couseae.repos.ApiMasterRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiMasterService {
    private final ApiMasterRepo apiMasterRepository;
    private final MongoTemplate mongoTemplate;
    private final MerchantAggregatorServ merchantAggregatorServ;

    public ApiRoutingContext getApiMaster(User usr, String targetType , String targetApiName) {

        log.info("IN getApiMaster() for user={} targetType={} targetApiName={}", usr.getUserId(), targetType, targetApiName);
        Optional<MerchantAggregatorMapping> agglst = merchantAggregatorServ.getMerAggregatorByActive(usr.getUserId(),targetType);

        if (agglst == null || agglst.isEmpty()) {
            log.warn("⚠️ No active aggregator mapping found for userId={}", usr.getUserId());
            return null;
        }
        MerchantAggregatorMapping topAgg = agglst.get();


        // 🧩 Fetch API Master for the aggregator
        log.info("🟢 Using aggregator={} for {} environment ", topAgg.getAggregatorCode(), topAgg.getEnvironment());
        List<ApiMaster> apilst = merchantAggregatorServ.getApiMasterByAggregatorCodeAndType(topAgg.getAggregatorCode(), topAgg.getEnvironment());
        log.info("the api list is {}", apilst.size());
        if (apilst == null || apilst.isEmpty()) {
            log.warn("⚠️ No API master configuration found for aggregator={}", topAgg.getAggregatorCode());
            return null;
        }

        ApiMaster apiMaster = apilst.stream()
                .filter(api -> targetApiName.equalsIgnoreCase(api.getApiName()))
                .filter(api -> targetType.equalsIgnoreCase(api.getType()))
                .findFirst()
                .orElse(null);
        if (apiMaster == null) {
            log.warn("⚠️ No matching API found for name={} and type={} under aggregator={}", targetApiName, targetType, topAgg.getAggregatorCode());
            return null;
        }
        log.info("API Master found for user={} targetType={} targetApiName={} ", usr.getUserId(), targetType, targetApiName);
        return new ApiRoutingContext(topAgg, apiMaster);

    }
    public ApiMaster getApiMasterForBalance(String aggrigatorCode,String environment, String targetType , String targetApiName) {

        log.info("IN getApiMaster() for Aggrigator={} targetType={} targetApiName={}", aggrigatorCode, targetType, targetApiName);
        // 🧩 Fetch API Master for the aggregator
        log.info("🟢 Using aggregator={} for {} environment ", aggrigatorCode, environment);
        List<ApiMaster> apilst = merchantAggregatorServ.getApiMasterByAggregatorCodeAndType(aggrigatorCode, environment);
        log.info("the api list is {}", apilst.size());
        if (apilst == null || apilst.isEmpty()) {
            log.warn("⚠️ No API master configuration found for aggregator={}", aggrigatorCode);
            return null;
        }

        ApiMaster apiMaster = apilst.stream()
                .filter(api -> targetApiName.equalsIgnoreCase(api.getApiName()))
                .filter(api -> targetType.equalsIgnoreCase(api.getType()))
                .findFirst()
                .orElse(null);
        if (apiMaster == null) {
            log.warn("⚠️ No matching API found for name={} and type={} under aggregator={}", targetApiName, targetType, aggrigatorCode);
            return null;
        }
        log.info("API Master found for aggrigatorCode={} targetType={} targetApiName={} ", aggrigatorCode, targetType, targetApiName);
        return apiMaster;

    }
    public ApiMaster getApiMasterByAggregatorId(String aggrigatorId,String targetApiName,String targetType){
        Optional<MerchantAggregatorMapping> merchantAggregtorMapping=merchantAggregatorServ.getMerAggregatorById(aggrigatorId);
        if(!merchantAggregtorMapping.isPresent()){
            log.info("⚠️ No Aggregator found for this order");

            return null;
        }
        MerchantAggregatorMapping merAggMap=merchantAggregtorMapping.get();
        List<ApiMaster> apilst = merchantAggregatorServ.getApiMasterByAggregatorCodeAndType(merAggMap.getAggregatorCode(),merAggMap.getEnvironment());

        if (apilst == null || apilst.isEmpty()) {
            log.warn("⚠️ No API master configuration found for aggregator={}", merAggMap.getAggregatorCode());
            return null;
        }

        // 🧩 Filter for CREATE_SESSION & PAYIN


        ApiMaster apimstr = apilst.stream()
                .filter(api -> targetApiName.equalsIgnoreCase(api.getApiName()))
                .filter(api -> targetType.equalsIgnoreCase(api.getType()))
                .findFirst()
                .orElse(null);

        if (apimstr == null) {
            log.warn("⚠️ No matching API found for name={} and type={} under aggregator={}",
                    targetApiName, targetType, merAggMap.getAggregatorCode());
            return null;
        }
    return apimstr;
    }
    public ApiMaster saveApimaster(ApiMaster apiMaster){
        return apiMasterRepository.save(apiMaster);
    }
    public ApiMaster updateApimaster(ApiMaster apiMaster) {
        return apiMasterRepository.save(apiMaster);
    }
    public ApiMaster getApimasterById(String id) {
        return apiMasterRepository.findById(id).orElse(null);
    }
    public List<ApiMaster> getAllApimasters() {
        return apiMasterRepository.findAll();
    }
    public List<String> getAllAcquirerCodes() {
        return mongoTemplate.query(ApiMaster.class)
                .distinct("aggregatorCode")
                .as(String.class)
                .all();
    }
}
