package com.pv.couseae.controller;

import com.pv.couseae.entities.PayoutIpWhitelist;
import com.pv.couseae.entities.PayoutSettings;
import com.pv.couseae.entities.User;
import com.pv.couseae.services.UserPayoutDetailsService;
import com.pv.couseae.utill.ResponseModel;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
//@CrossOrigin
@RequestMapping("payout")
@AllArgsConstructor
public class UserPayoutDetailsController {
    private UserPayoutDetailsService payoutDetailsService;
//-------------- Payout Settings -------------------------
    @PostMapping("settings")
    ResponseEntity<?> addPayoutSettings(@Valid @RequestBody PayoutSettings payoutSettings){
        PayoutSettings isPayoutSettings = this.payoutDetailsService.getByUser(payoutSettings.getUser());
        if (isPayoutSettings != null){
            return ResponseModel.customValidations("Settings", "Payout Settings already defined");
        }
        this.payoutDetailsService.addSettings(payoutSettings);
        return ResponseModel.success("Payout Settings done");
    }

    @GetMapping("settings/{userId}")
    ResponseEntity<?> allPayoutSettings(@PathVariable String userId){
        PayoutSettings payoutSettings = this.payoutDetailsService.getByUser(new User(userId));
        return ResponseModel.success("Payout Settings", payoutSettings);
    }

    //-------------- ipWhiteList -------------------------
    @PostMapping("ipWhiteList")
    ResponseEntity<?> addPayoutIPWhiteList(@Valid @RequestBody PayoutIpWhitelist ipWhitelist){
        PayoutIpWhitelist isIpWhitelist = this.payoutDetailsService.getByUserAndIp(ipWhitelist.getUser(),ipWhitelist.getIpAddress());
        if (isIpWhitelist != null){
            return ResponseModel.customValidations("IP", ipWhitelist.getIpAddress()+" is already added in whiteList");
        }
        this.payoutDetailsService.addIpAddress(ipWhitelist);
        return ResponseModel.success("IP Added in white Listed");
    }

    @GetMapping("ipWhiteList/{userId}")
    ResponseEntity<?> allIPAddress(@PathVariable String userId){
        List<PayoutIpWhitelist> ipWhitelist = this.payoutDetailsService.getAllIPAddressByUser(new User(userId));
        return ResponseModel.success("IP White list", ipWhitelist);
    }
    @DeleteMapping("ipWhiteList")
    ResponseEntity<?> removeIP(@Valid @RequestBody PayoutIpWhitelist ipWhitelist){
        PayoutIpWhitelist isIpWhitelist = this.payoutDetailsService.getByUserAndIp(ipWhitelist.getUser(),ipWhitelist.getIpAddress());
        this.payoutDetailsService.removeIp(isIpWhitelist.getPayoutIpWhitelistId());
        return ResponseModel.deleted();
    }

//    //-------------- Initial Money -------------------------
//    @PostMapping("initialMoney")
//    ResponseEntity<?> addPayoutInitialMoney(@Valid @RequestBody PayoutMoney payoutMoney){
//        PayoutMoney isPayoutMoney = this.payoutDetailsService.getMoneyByUser(payoutMoney.getUser());
//        if (isPayoutMoney != null){
//            return ResponseModel.customValidations("IP", " Initial money is already added ");
//        }
//        this.payoutDetailsService.addInitialMoney(payoutMoney);
//        return ResponseModel.success("Initial Money added");
//    }
//
//    @GetMapping("initialMoney/{userId}")
//    ResponseEntity<?> getInitialMoney(@PathVariable String userId){
//        PayoutMoney payoutMoney = this.payoutDetailsService.getMoneyByUser(new User(userId));
//        return ResponseModel.success("Initial Money", payoutMoney);
//    }
}
