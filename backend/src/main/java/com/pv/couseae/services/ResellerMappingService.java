package com.pv.couseae.services;

import com.pv.couseae.entities.ResellerMapping;
import com.pv.couseae.entities.User;
import com.pv.couseae.utill.SearchRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ResellerMappingService {
    void addResellerMapping(ResellerMapping resellerMapping);

    ResellerMapping getResellerMapping(User reseller, User merchant);

    ResellerMapping getResellerMappingById(String resellerMerchantId);

    void deleteResellerMapping(String id);

    Page<ResellerMapping> searchResellerMapping(SearchRequest searchRequest);

    List<User> getAllMappedMerchantByReseller(User user);
}
