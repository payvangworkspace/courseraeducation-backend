package com.pv.couseae.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pv.couseae.Dtos.Crypto.CryptoOrderRequest;
import com.pv.couseae.Dtos.Crypto.CryptoOrderResponse;
import com.pv.couseae.Dtos.MerchantCreateResponse;
import com.pv.couseae.entities.MerchantCryptoConfig;
import com.pv.couseae.entities.MerchantCryptoKeys;
import com.pv.couseae.entities.PayinRequestCrypto;
import com.pv.couseae.entities.User;
import com.pv.couseae.httpServices.HttpService;
import com.pv.couseae.httpServices.HttpServices;
import com.pv.couseae.model.PayinReqCrypto;
import com.pv.couseae.model.PayinResponseCrypto;
import com.pv.couseae.repos.CryptoConfigRepo;
import com.pv.couseae.repos.MerchantCryptoKeysRepo;
import com.pv.couseae.repos.PayinReqCryptoRepo;
import com.pv.couseae.utill.ResponseModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CryptoService {
    private final CryptoConfigRepo cryptoConfigRepo;
    private final MerchantCryptoKeysRepo keysRepo;
    private final HttpServices httpServices;
    private final HttpService httpService;
    private final PayinReqCryptoRepo payinRepo;

    public MerchantCryptoConfig SaveConfig(MerchantCryptoConfig req){
        return cryptoConfigRepo.save(req);
    }
    public void SaveConfigAll(List<MerchantCryptoConfig> req){
        cryptoConfigRepo.saveAll(req);
    }
    public List<MerchantCryptoConfig> listCryptoConfig(){
        return cryptoConfigRepo.findAll();
    }
    public List<MerchantCryptoConfig> listCryptoConfigByMerchant(String merchantId){
        return cryptoConfigRepo.findByMerchantId(merchantId);
//        return cryptoConfigRepo.findByMerchantIdAndStatusTrue(merchantId);
    }
    public List<MerchantCryptoConfig> listCryptoConfigByActiveMerchant(String merchantId){
        return cryptoConfigRepo.findByMerchantIdAndStatusTrue(merchantId);
    }

    public void SaveMerchantKeysFroApi(String merchantName,String merchantId){

        MerchantCryptoKeys keysDtls=keysRepo.findByMerchantId(merchantId).orElse(null);
        if(keysDtls!=null){

        }else{
            MerchantCreateResponse.DataNode mecrhantkeys =httpServices.GetCryptoMerchantOnboarding(merchantName,merchantId);
            if(mecrhantkeys!=null){
                MerchantCryptoKeys req=new MerchantCryptoKeys();
                req.setMerchantId(merchantId);
                req.setCryptoMerchantId(mecrhantkeys.getMerchantId());
                req.setApiSecret(mecrhantkeys.getApiSecret());
                req.setApiKey(mecrhantkeys.getApiKey());
                req.setActive(true);
                keysRepo.save(req);
            }
        }
    }
    public ResponseEntity<Object> CreateCryptoOrder(PayinReqCrypto payineq, User usr, String key) throws JsonProcessingException {

        PayinRequestCrypto payinEntity=new PayinRequestCrypto();

        payinEntity.setOrderId(payineq.getOrderId());
        payinEntity.setFiatAmount(payineq.getFiatAmount());
        payinEntity.setCryptoType(payineq.getCoinType());
        payinEntity.setNetworkType(payineq.getChainType());
        payinEntity.setFiatCurrency(payineq.getFiatCurrCode());
        payinEntity.setMerchantId(payineq.getMerchantId());
        payinEntity.setWalletAddress(payineq.getWalletAddress());
        payinEntity.setReversed(false);
        payinEntity.setSettled(false);
        payinEntity.setStatus("INITIATED");
        payinEntity.setCreatedDate(LocalDateTime.now());

        PayinRequestCrypto savedPayin=payinRepo.save(payinEntity);
        PayinResponseCrypto retRes=new PayinResponseCrypto();

        retRes.setAmount(payineq.getFiatAmount()+"");
        retRes.setOrderId(payineq.getOrderId());
        retRes.setEmailId(payineq.getEmailId());
        retRes.setCoinType(payineq.getCoinType());


        CryptoOrderRequest req=new CryptoOrderRequest();
        req.setEmail(payineq.getEmailId());
        req.setExternalOrderId(payineq.getOrderId());
        req.setFiatAmount(payineq.getFiatAmount());
        req.setInvoiceId("Inv-"+payineq.getOrderId());
        req.setFirstName(payineq.getFirstname());
        req.setLastName(payineq.getLastname());
        req.setFiatCurrency(payineq.getFiatCurrCode());
        req.setNetworkName(payineq.getChainType());
        req.setTokenSymbol(payineq.getCoinType());
        req.setSessionExpiryMinutes(15);


        String jsonString=httpService.createCryptoOrder(req,key);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        CryptoOrderResponse response =mapper.readValue(jsonString, CryptoOrderResponse.class);

        log.info("Order ID: {}", response.getData().getId());
        log.info("Payment Link: {}", response.getData().getPaymentLinkUrl());
        retRes.setCryptoOrderId(response.getData().getId());
        retRes.setLinkexpirytime(response.getData().getSessionExpiresAt()+"");
        retRes.setPaymentlink(response.getData().getPaymentLinkUrl());
        retRes.setStatusCode(response.getStatus()+"");

        savedPayin.setStatus("INPROGRESS");
        savedPayin.setCryptoOrderId(retRes.getCryptoOrderId());
        savedPayin.setPaymentLink(retRes.getPaymentlink());

        payinRepo.save(savedPayin);

        return ResponseModel.success("Order Data",retRes);
    }

}
