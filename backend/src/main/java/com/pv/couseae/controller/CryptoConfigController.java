package com.pv.couseae.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pv.couseae.entities.MerchantCryptoConfig;
import com.pv.couseae.entities.MerchantCryptoKeys;
import com.pv.couseae.repos.MerchantCryptoKeysRepo;
import com.pv.couseae.services.CryptoService;
import com.pv.couseae.utill.ResponseModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/CryptoConfig")
@RequiredArgsConstructor
public class CryptoConfigController {

    private final CryptoService cryptoService;
    private final MerchantCryptoKeysRepo keysRepo;

    @PostMapping("/SaveCryptoConfig")
    public ResponseEntity<?> saveConfig(@RequestBody MerchantCryptoConfig req){

        cryptoService.SaveConfig(req);

     return ResponseModel.success("Data Saved Successfullly..!!!");
   }
    @PostMapping("/updateCryptoConfig")
    public ResponseEntity<?> updateConfig(@RequestBody MerchantCryptoConfig req) {

        List<MerchantCryptoConfig> lst = cryptoService.listCryptoConfigByActiveMerchant(req.getMerchantId());
        List<MerchantCryptoConfig> activeList = lst.stream().filter(MerchantCryptoConfig::isStatus).collect(Collectors.toList());
        if (activeList.size() > 0){
            for (MerchantCryptoConfig conf : activeList) {
                conf.setStatus(false);
            }
           cryptoService.SaveConfigAll(activeList);
        }
        cryptoService.SaveConfig(req);

        return ResponseModel.success("Data Saved Successfullly..!!!");
    }
    @GetMapping("/ListCryptoConfig")
    public ResponseEntity<?> getAllConfig(){
        return ResponseModel.success("Config All Data",cryptoService.listCryptoConfig());
    }

    @GetMapping("/ActiveMerchantCryptoConfig/{merchantId}")
    public ResponseEntity<?> getActiveConfig(@PathVariable String merchantId){
        return ResponseModel.success("Config Data for Order",cryptoService.listCryptoConfigByActiveMerchant(merchantId));
    }
    @GetMapping("/MerchantCryptoConfig/{merchantId}")
    public ResponseEntity<?> getMerchantConfig(@PathVariable String merchantId){
        return ResponseModel.success("Config Data",cryptoService.listCryptoConfigByMerchant(merchantId));
    }
    @PostMapping("/SaveCryptoKeys")
    public ResponseEntity<?> saveConfig(@RequestBody String request) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode jsonNode = mapper.readTree(request);

        String merchantName = jsonNode.get("name").asText();
        String merchantId = jsonNode.get("merchantId").asText();

        cryptoService.SaveMerchantKeysFroApi(merchantName,merchantId);

        return ResponseModel.success("Data Saved Successfully..!!!");
    }
    @GetMapping("/GetCryptoKeys/{merchantId}")
    public ResponseEntity<?> GetConfig(@PathVariable String merchantId){

        MerchantCryptoKeys resp=keysRepo.findByMerchantId(merchantId).orElse(null);

        return ResponseModel.success("Data Retrieved..",resp);
    }

}
