package com.pv.couseae.controller;

import com.pv.couseae.entities.MerchantCryptoWallet;
import com.pv.couseae.entities.MerchantWallet;
import com.pv.couseae.services.CryptoWalletService;
import com.pv.couseae.services.UserService;
import com.pv.couseae.services.WalletService;
import com.pv.couseae.utill.ResponseModel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
//@CrossOrigin
@RequestMapping("wallet")
@AllArgsConstructor
public class WalletController {


    @Autowired
    private WalletService walletService;
    @Autowired
    private CryptoWalletService cryptoWalletService;
    @Autowired
    private UserService userService;

    // List all wallets
    @GetMapping("/walletList")
    public ResponseEntity<?> getAllWallets() {
        List<MerchantWallet> wallets = walletService.getAllWallets();
        return ResponseModel.success("All Merchant Wallets",wallets);
    }
    // Save or update wallet
    @PostMapping("/savewallet")
    public ResponseEntity<?> saveWallet(@RequestBody MerchantWallet wallet) {

        boolean isMerchant = userService.isMerchant(wallet.getMerchantId());
        if(!isMerchant){
            return ResponseModel.error("Merchant not found");
        }
        MerchantWallet savedWallet = walletService.saveWallet(wallet);
        return ResponseModel.success("Wallet Saved Successfully",savedWallet);
    }
    @PostMapping("/creditwallet")
    public ResponseEntity<?> saveWallet(@RequestParam String merchantId, @RequestParam double amount   ) {
        boolean isMerchant = userService.isMerchant(merchantId);
        if(!isMerchant){
            return ResponseModel.error("Merchant not found");
        }
        MerchantWallet savedWallet = walletService.creditBalance(merchantId, BigDecimal.valueOf(amount));
        return ResponseModel.success("Wallet Credited Successfully",savedWallet);
    }

    // Get wallet by merchantId
    @GetMapping("/getWalletByMerchantId/{merchantId}")
    public ResponseEntity<?> getWallet(@PathVariable String merchantId) {
        MerchantWallet wallet = walletService.getWallet(merchantId);
        if (wallet != null) {
            return ResponseModel.success("Wallet Data", wallet);
        } else {
            return ResponseModel.error("Merchant not found");
        }
    }
    @GetMapping("/cryptoWalletList")
    public ResponseEntity<?> getAllCryptoWallets() {
        List<MerchantCryptoWallet> wallets = cryptoWalletService.getAllCryptoWallets();
        return ResponseModel.success("All Merchant Wallets",wallets);
    }
    @GetMapping("/getCryptoWalletByMerchantId/{merchantId}")
    public ResponseEntity<?> getCryptoWallet(@PathVariable String merchantId) {
        List<MerchantCryptoWallet> wallet = cryptoWalletService.getCryptoWallet(merchantId);
        if (wallet != null) {
            return ResponseModel.success("Crypto Wallet Data", wallet);
        } else {
            return ResponseModel.error("Merchant wallet not found");
        }
    }



}
