package com.pv.couseae.auth;

import com.pv.couseae.Dtos.CreateKeyReq;
import com.pv.couseae.entities.IpApiKeyInfo;
import com.pv.couseae.repos.IpApiKeyRepo;
import com.pv.couseae.services.IPEncryptionService;
import com.pv.couseae.utill.IpBoundKeyGenerator;
import com.pv.couseae.utill.ResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/keys")
public class KeyAdminController {

    private final IpApiKeyRepo repo;
    private final IpBoundKeyGenerator keyGen;
    private final PasswordEncoder passwordEncoder; // BCryptPasswordEncoder
    private final IPEncryptionService ipEncryptionService;

    public KeyAdminController(IpApiKeyRepo repo, IpBoundKeyGenerator keyGen, PasswordEncoder passwordEncoder, IPEncryptionService ipEncryptionService) {
        this.repo = repo;
        this.keyGen = keyGen;
        this.passwordEncoder = passwordEncoder;
        this.ipEncryptionService = ipEncryptionService;
    }

    @PostMapping("/ListAllKeys")
    public ResponseEntity<?> listAllKeys() {
        log.info("List all keys");
        List<IpApiKeyInfo> lstipApiKeyInfo =repo.findAll();
        return ResponseModel.success("All IP Keys",lstipApiKeyInfo);
    }
    @PostMapping("/createIPKey")
    public ResponseEntity<?> create(@RequestBody CreateKeyReq req) {
        log.info("create key req :"+req.toString()+" and merchat id - "+req.getMerchantId());
        // req.allowedIp should be validated (format, optionally GeoIP)
        String allowedIp = req.allowedIp.trim();

//        List<IpApiKeyInfo> ipApiKeyInfo = repo.findByAllowedIps(allowedIp).stream()
//                .filter(i->i.getActive()).toList();
        List<IpApiKeyInfo> ipApiKeyInfo = repo.findByAllowedIpsAndMerchantId(allowedIp,req.getMerchantId()).stream()
                .filter(i->i.getActive()).toList();
        if (ipApiKeyInfo !=null && ipApiKeyInfo.size() > 0) {
            return ResponseModel.error("IP Key already exists for this IP address");
        }

        // generate token bound to allowedIp
        IpBoundKeyGenerator.Pair<String,String> p = keyGen.generateTokenForIp(allowedIp);
        String nonce = p.a;
        String token = p.b; // HMAC result

        // store hash (we store nonce + "." + token)
        String stored = nonce + "." + token;
        String hash = passwordEncoder.encode(stored);

        IpApiKeyInfo entity = new IpApiKeyInfo();
        entity.setMerchantId(req.getMerchantId());
        entity.setKeyHash(hash);
        entity.setActive(true);
        entity.setAllowedIps(allowedIp);
        entity.setCreatedDate(LocalDateTime.now());
        repo.save(entity);

//        String keyId = String.valueOf(entity.getAllowedIps());
//        String returnedKey = keyId + "." + nonce + "." + token; // show once
        String keyId=ipEncryptionService.encrypt(req.getMerchantId());
        String returnedKey =  nonce + "." + token+"."+keyId; // show once
        log.info("Return key :-"+returnedKey);
        Map<String,String> resp = Map.of("apiKey", returnedKey, "note", "Store securely. Shown only once.");
        return ResponseModel.success("IP Key created successfully", resp);
    }


    @GetMapping("/GetIpKeyList/{merchantId}")
    public ResponseEntity<?> getMerchantIPKeyList(@PathVariable String merchantId ) {
        if(merchantId!=null &&  merchantId!=null){
            log.info("Get IP Key List for merchantId :"+merchantId);

            return ResponseModel.success("IP Key List",repo.findByMerchantId(merchantId));
        } else{
            return ResponseModel.error("MerchantId is Mandatory in Request");
        }
    }
    @GetMapping("/GetIpKeyList")
    public ResponseEntity<?> getIPKeyList(@RequestBody CreateKeyReq req) {
        if(req!=null &&  req.getMerchantId()!=null){
            log.info("Get IP Key List for merchantId :"+req.getMerchantId());

            return ResponseModel.success("IP Key List",repo.findByMerchantId(req.getMerchantId()));
        }else if(req!=null &&  req.getAllowedIp()!=null) {
            log.info("Get IP Key List for allowedIp :"+req.getAllowedIp());
            return ResponseModel.success("IP Key List",repo.findByAllowedIps(req.getAllowedIp()));
        }else{
            log.info("Get All IP Key List");
          return  ResponseModel.success("IP Key List",repo.findAll());
        }

    }


    @GetMapping("/TestIPKey")
    public ResponseEntity<?> TestIPKey() {
        log.info("In side TestIPKey :" );
    return ResponseEntity.ok("API Key tested.......");
    }

}