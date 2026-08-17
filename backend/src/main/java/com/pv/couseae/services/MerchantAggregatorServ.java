package com.pv.couseae.services;

import com.pv.couseae.entities.ApiMaster;
import com.pv.couseae.entities.MerchantAggregatorMapping;
import com.pv.couseae.repos.ApiMasterRepo;
import com.pv.couseae.repos.MerchantAggregatorMappingRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class MerchantAggregatorServ {
   private final ApiMasterRepo apiMasterRepo;
   private final MerchantAggregatorMappingRepo merAggregatorRepo;
    @Autowired
    private MongoTemplate mongoTemplate;

    public List<ApiMaster> getApiMasterByAggregatorCode(String aggregatorCode) {
        return apiMasterRepo.findByAggregatorCode(aggregatorCode).stream().filter(ApiMaster::isActive).collect(Collectors.toList());
    }
    public Optional<ApiMaster> getApiMasterById(String id) {
        return apiMasterRepo.findById(id);
    }
   public List<ApiMaster> getApiMasterByAggregatorCodeAndType(String aggregatorCode, String type) {
  return apiMasterRepo.findByAggregatorCodeAndEnvironment(aggregatorCode, type).stream().filter(ApiMaster::isActive).collect(Collectors.toList());
      //return apiMasterRepo.findByAggregatorCodeAndType(aggregatorCode, type);
   }
  public Optional<MerchantAggregatorMapping> getMerAggregatorById(String id) {
       return merAggregatorRepo.findById(id);
   }
   public List<MerchantAggregatorMapping> getMerAggregatorByEnv_(String merchantId, String environment) {
       return merAggregatorRepo.findByMerchantIdAndEnvironment(merchantId, environment).stream()
               .filter(MerchantAggregatorMapping::isActive)
               .sorted(Comparator.comparing(MerchantAggregatorMapping::getPriority))
               .collect(Collectors.toList());
   }
    public List<MerchantAggregatorMapping> getMerAggregatorByEnv(String merchantId, String environment) {
        Query query = new Query();

        query.addCriteria(Criteria.where("merchantId").is(merchantId)
                .and("environment").is(environment)
                .and("active").is(true));

        query.with(Sort.by(Sort.Direction.ASC, "priority"));
//
        return mongoTemplate.find(query, MerchantAggregatorMapping.class);
    }
    public List<MerchantAggregatorMapping> getMerAggregatorByActive_(String merchantId,String type) {
        Query query = new Query();

        query.addCriteria(Criteria.where("merchantId").is(merchantId)
                .and("active").is(true));

        query.with(Sort.by(Sort.Direction.ASC, "priority"));
//
        return mongoTemplate.find(query, MerchantAggregatorMapping.class);
    }
    public Optional<MerchantAggregatorMapping> getMerAggregatorByActive(String merchantId,String txnType) {

        Query query = Query.query(Criteria.where("merchantId").is(merchantId)
                .and("active").is(true).and("txnType").is(txnType)
        ).with(Sort.by(Sort.Direction.ASC, "priority")).limit(1);

        List<MerchantAggregatorMapping> list =mongoTemplate.find(query, MerchantAggregatorMapping.class);

        return list.stream().findFirst();
    }
}
