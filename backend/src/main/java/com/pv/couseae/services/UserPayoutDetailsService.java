package com.pv.couseae.services;

import com.pv.couseae.entities.PayoutIpWhitelist;
import com.pv.couseae.entities.PayoutSettings;
import com.pv.couseae.entities.User;

import java.util.List;

public interface UserPayoutDetailsService {
    PayoutSettings getByUser(User user);

    void addSettings(PayoutSettings payoutSettings);

    PayoutIpWhitelist getByUserAndIp(User user, String ipAddress);

    void addIpAddress(PayoutIpWhitelist ipWhitelist);

    List<PayoutIpWhitelist> getAllIPAddressByUser(User user);

    void removeIp(String payoutIpWhitelistId);

//    PayoutMoney getMoneyByUser(User user);
//
//    void addInitialMoney(PayoutMoney payoutMoney);

}
