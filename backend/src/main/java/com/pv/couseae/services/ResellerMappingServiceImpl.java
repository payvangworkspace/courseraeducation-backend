package com.pv.couseae.services;

import com.pv.couseae.entities.ResellerMapping;
import com.pv.couseae.entities.User;
import com.pv.couseae.repos.ResellerMappingRepo;
import com.pv.couseae.utill.SearchRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ResellerMappingServiceImpl implements ResellerMappingService{
    private ResellerMappingRepo mappingRepo;

    @Override
    public void addResellerMapping(ResellerMapping resellerMapping) {
        this.mappingRepo.save(resellerMapping);
    }

    @Override
    public ResellerMapping getResellerMapping(User reseller, User merchant) {
        return this.mappingRepo.findByMerchantIdAndResellerId(reseller,merchant);
    }

    @Override
    public ResellerMapping getResellerMappingById(String resellerMerchantId) {
        return this.mappingRepo.findById(resellerMerchantId).orElse(null);
    }

    @Override
    public void deleteResellerMapping(String id) {
        this.mappingRepo.deleteById(id);
        this.mappingRepo.delete(new ResellerMapping(id));
    }

    @Override
    public Page<ResellerMapping> searchResellerMapping(SearchRequest searchRequest) {
        Pageable pageable = PageRequest.of(searchRequest.getStart(), searchRequest.getSize());
        if(searchRequest.getUserName().isEmpty()){
            if(searchRequest.getKeyword().isEmpty()) return this.mappingRepo.findAll(pageable);
            else return this.mappingRepo.findAllByMerchantUserNameLikeIgnoreCaseOrMerchantFullNameLikeIgnoreCase(searchRequest.getKeyword(),searchRequest.getKeyword(),pageable);
        } else {
            if(searchRequest.getKeyword().isEmpty()) return this.mappingRepo.findAllByResellerId(new User(searchRequest.getUserName()),pageable);
            else return this.mappingRepo.findAllByResellerIdAndMerchantUserNameLikeIgnoreCase(new User(searchRequest.getUserName()),searchRequest.getKeyword(),pageable);
        }

    }

    @Override
    public List<User> getAllMappedMerchantByReseller(User user) {
        List<ResellerMapping> resellerMappings = this.mappingRepo.findByResellerId(user);
        List<User> users = new ArrayList<>();
        for (ResellerMapping items:resellerMappings)
            users.add(items.getMerchantId());
        return users;
    }
}
