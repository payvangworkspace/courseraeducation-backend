package com.pv.couseae.services;

import com.pv.couseae.entities.PayoutIpWhitelist;
import com.pv.couseae.entities.PayoutSettings;
import com.pv.couseae.entities.User;
import com.pv.couseae.repos.PayoutIpWhitelistRepo;
import com.pv.couseae.repos.PayoutSettingsRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserPayoutDetailsServiceImpl implements UserPayoutDetailsService{
    private PayoutSettingsRepo payoutSettingsRepo;
    private PayoutIpWhitelistRepo payoutIpWhitelistRepo;
//    private PayoutMoneyRepo payoutMoneyRepo;

    @Override
    public PayoutSettings getByUser(User user) {
        return this.payoutSettingsRepo.findByUser(user);
    }

    @Override
    public void addSettings(PayoutSettings payoutSettings) {
        this.payoutSettingsRepo.save(payoutSettings);
    }

    @Override
    public PayoutIpWhitelist getByUserAndIp(User user, String ipAddress) {
        return this.payoutIpWhitelistRepo.findByUserAndIpAddress(user, ipAddress);
    }

    @Override
    public void addIpAddress(PayoutIpWhitelist ipWhitelist) {
        this.payoutIpWhitelistRepo.save(ipWhitelist);
    }

    @Override
    public List<PayoutIpWhitelist> getAllIPAddressByUser(User user) {
        return this.payoutIpWhitelistRepo.findAllByUser(user);
    }

    @Override
    public void removeIp(String payoutIpWhitelistId) {
        this.payoutIpWhitelistRepo.deleteById(payoutIpWhitelistId);
    }

//    @Override
//    public PayoutMoney getMoneyByUser(User user) {
//        return this.payoutMoneyRepo.findByUser(user);
//    }
//
//    @Override
//    public void addInitialMoney(PayoutMoney payoutMoney) {
//        this.payoutMoneyRepo.save(payoutMoney);
//    }

}
