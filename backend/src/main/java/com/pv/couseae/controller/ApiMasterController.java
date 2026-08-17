package com.pv.couseae.controller;

import com.pv.couseae.entities.ApiMaster;
import com.pv.couseae.entities.MerchantAggregatorMapping;
import com.pv.couseae.repos.MerchantAggregatorMappingRepo;
import com.pv.couseae.services.ApiMasterService;
import com.pv.couseae.utill.ResponseModel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/apimasters")
@AllArgsConstructor
public class ApiMasterController {
    private final ApiMasterService apiMasterService;
    private final MerchantAggregatorMappingRepo merchantAggregatorMapping;

    @PostMapping("/saveapi")
    public ApiMaster saveApiMaster(@RequestBody ApiMaster apiMaster) {
        apiMaster.setCreatedAt(LocalDateTime.now());
        return apiMasterService.saveApimaster(apiMaster);
    }
    @PostMapping("/updateapi")
    public ApiMaster UpdateApiMaster(@RequestBody ApiMaster apiMaster) {
        if(apiMaster.getId()!= null){
             log.info("Update API Master By ID:{}",apiMaster.getId());
           ApiMaster apimasterUpdate = apiMasterService.getApimasterById(apiMaster.getId());
            apimasterUpdate.setBaseUrl(apiMaster.getBaseUrl());
            apimasterUpdate.setClientId(apiMaster.getClientId());
            apimasterUpdate.setEndpoint(apiMaster.getEndpoint());
            apimasterUpdate.setMerchantId(apiMaster.getMerchantId());
            apimasterUpdate.setResponsekey(apiMaster.getResponsekey());
            apimasterUpdate.setSecretKey(apiMaster.getSecretKey());
            apimasterUpdate.setWebhoockUrl(apiMaster.getWebhoockUrl());
            apimasterUpdate.setActive(apiMaster.isActive());
            apimasterUpdate.setUpdatedAt(LocalDateTime.now());

            return apiMasterService.updateApimaster(apimasterUpdate);
        }else{
             log.info("ID not found, Save API Master");
            return new ApiMaster();
        }
    }
    @GetMapping("/GetAllApi")
    public ResponseEntity<?> getapimaster(){
         log.info("Get All API Master");
        return ResponseModel.success("API Master", apiMasterService.getAllApimasters());
    }
    @GetMapping("/GetAcquirerCodes")
    public ResponseEntity<?> getAcquirerCodeApiMaster(){
        log.info("Get All API Master");
        return ResponseModel.success("API Master", apiMasterService.getAllAcquirerCodes());
    }
    @GetMapping("/GetApiByid/{id}")
    public ResponseEntity<?> getapimasterByid(@PathVariable String id){
         log.info("Get API Master By ID:{}",id);
        return ResponseModel.success("API Master", apiMasterService.getApimasterById(id));
    }
    @PostMapping("/savemerchantAggregatormapping")
    public MerchantAggregatorMapping saveMerchantAggregatorMapping(@RequestBody MerchantAggregatorMapping apiMaster) {
        apiMaster.setCreatedOn(LocalDateTime.now());
        return merchantAggregatorMapping.save(apiMaster);
    }
    @PostMapping("/updatemerchantAggregatormapping")
    public MerchantAggregatorMapping updateMerchantAggregatorMapping(@RequestBody MerchantAggregatorMapping apiMaster) {

        if(apiMaster.getId()!= null){
             log.info("Update Merchant Aggregator Mapping By ID:{}",apiMaster.getId());
             MerchantAggregatorMapping merchantAggregatorMappingUpdate = merchantAggregatorMapping.findById(apiMaster.getId()).get();
            merchantAggregatorMappingUpdate.setPriority(apiMaster.getPriority());
            merchantAggregatorMappingUpdate.setEnvironment(apiMaster.getEnvironment());
            merchantAggregatorMappingUpdate.setTxnType(apiMaster.getTxnType());
            merchantAggregatorMappingUpdate.setActive(apiMaster.isActive());
            merchantAggregatorMappingUpdate.setUpdatedOn(LocalDateTime.now());
            return merchantAggregatorMapping.save(merchantAggregatorMappingUpdate);
        }else{
             log.info("Save Merchant Aggregator Mapping");
            return new MerchantAggregatorMapping();
        }

    }
    @GetMapping("/GetMerchantAggregatorMapping/{merchantId}")
    public ResponseEntity<?> getMerchantAggregatorMapping(@PathVariable String merchantId){
         log.info("Get Merchant Aggregator Mapping By Merchant ID:{}",merchantId);
        return ResponseModel.success("Mechant Aggregator Mapping data", merchantAggregatorMapping.findByMerchantId(merchantId));
    }

}
