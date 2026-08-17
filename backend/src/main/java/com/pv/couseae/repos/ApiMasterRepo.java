package com.pv.couseae.repos;

import com.pv.couseae.entities.ApiMaster;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiMasterRepo extends MongoRepository<ApiMaster, String> {
    List<ApiMaster> findByAggregatorCode(String aggcode);
    Optional<ApiMaster> findById(String id);
    List<ApiMaster> findByAggregatorCodeAndType(String aggcode, String type);
    List<ApiMaster> findByAggregatorCodeAndEnvironment(String aggcode, String type);
}
