package com.pv.couseae.repos;

import com.pv.couseae.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface UserRepo extends MongoRepository<User, String> {

    User findByUserId(String username);

    @Query("{ 'userId': ?0, 'appKey': ?1 }")
    User findByUserIdAndAppKeyCaseSensitive(String username, String appId);

    Page<User> findAllByRoleAndFullNameLikeIgnoreCaseOrderByFullName(String roleName, String keyword, Pageable pageable);

    Page<User> findAllByRoleOrderByFullName(String roleName, Pageable pageable);
    List<User> findByRoleOrderByFullName(String roleName);
    long countByRole(String roleName);
    Page<User> findAllByFullNameLikeIgnoreCaseOrderByFullName(String keyword, Pageable pageable);

    User findByAppKey(String appKey);

    User findByAppKeyAndSecretKey(String merchantAppId, String merchantSecretId);

    Page<User> findAllByRoleAndCreatedByOrderByFullName(String roleName, String user, Pageable pageable);

    Page<User> findAllByRoleAndFullNameLikeIgnoreCaseAndCreatedByOrderByFullName(String roleName, String keyword, String user, Pageable pageable);
}
