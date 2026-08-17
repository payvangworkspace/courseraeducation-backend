package com.pv.couseae.repos;

import com.pv.couseae.entities.PayoutIpWhitelist;
import com.pv.couseae.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PayoutIpWhitelistRepo extends MongoRepository<PayoutIpWhitelist, String> {
    PayoutIpWhitelist findByUserAndIpAddress(User user, String ipAddress);

    List<PayoutIpWhitelist> findAllByUser(User user);
}
