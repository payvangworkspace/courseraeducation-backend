package com.pv.couseae.auth;

import com.pv.couseae.services.RSAKeyService;
import com.pv.couseae.utill.RSAUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

@Slf4j
@RestController
//@CrossOrigin
@RequestMapping("/apiauth")
public class KeyController {

    @Autowired
    private RSAKeyService rsaKeyService;
    @Autowired
    private RSAUtil rsaUtil;

    @GetMapping("/publicKey")
    public String getPublicKey()  {
        return Base64.getEncoder().encodeToString(rsaKeyService.getPublicKey().getEncoded());
    }

}
