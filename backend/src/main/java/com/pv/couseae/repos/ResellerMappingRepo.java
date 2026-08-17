package com.pv.couseae.repos;

import com.pv.couseae.entities.ResellerMapping;
import com.pv.couseae.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ResellerMappingRepo extends MongoRepository<ResellerMapping, String> {
    ResellerMapping findByMerchantIdAndResellerId(User reseller, User merchant);

    Page<ResellerMapping> findAllByResellerId(User user, Pageable pageable);
    List<ResellerMapping> findByResellerId(User user);

    Page<ResellerMapping> findAllByMerchantUserNameLikeIgnoreCaseOrMerchantFullNameLikeIgnoreCase(String keyword, String keyword1, Pageable pageable);

    Page<ResellerMapping> findAllByResellerIdAndMerchantUserNameLikeIgnoreCase(User user, String keyword, Pageable pageable);
}
