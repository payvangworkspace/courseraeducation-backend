package com.pv.couseae.repos;

import com.pv.couseae.entities.UserActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserActivityRepo extends MongoRepository<UserActivity,String> {
//    Page<UserActivity> findAllByCreatedByAndCreatedDateBetween(String userName, Date dateFrom, Date dateTo);

    Page<UserActivity> findAllByCreatedBy(String userName, Pageable pageable);
}

