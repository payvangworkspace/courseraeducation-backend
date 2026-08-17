package com.pv.couseae.repos;

import com.pv.couseae.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepoDB extends MongoRepository<User, String> {
    //User findByUserId(String username);
    Optional<User> findByUserId(String username);

    User findByAppKeyAndSecretKey(String appKey, String secretKey);
}
