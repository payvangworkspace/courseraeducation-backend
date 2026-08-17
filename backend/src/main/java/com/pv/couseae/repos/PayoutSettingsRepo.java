package com.pv.couseae.repos;

import com.pv.couseae.entities.PayoutSettings;
import com.pv.couseae.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PayoutSettingsRepo extends MongoRepository<PayoutSettings, String> {
    PayoutSettings findByUser(User user);
}
