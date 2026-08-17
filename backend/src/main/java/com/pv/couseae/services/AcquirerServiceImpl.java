package com.pv.couseae.services;

import com.pv.couseae.entities.Acquirer;
import com.pv.couseae.repos.AcquirerRepo;
import com.pv.couseae.utill.SearchRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AcquirerServiceImpl implements AcquirerService {
    private AcquirerRepo acquirerRepo;

    @Override
    public void addNewAcquirer(Acquirer acquirer) {
        this.acquirerRepo.save(acquirer);
    }

    @Override
    public void updateAcquirer(Acquirer acquirer) {
        this.acquirerRepo.save(acquirer);
    }

    @Override
    public Page<Acquirer> geAllAcquirer(SearchRequest searchRequest) {
        Pageable pageable = PageRequest.of(searchRequest.getStart(), searchRequest.getSize());
        if (!searchRequest.getKeyword().isEmpty())
            return this.acquirerRepo.findAllByFullNameLikeIgnoreCaseOrAcquirerCodeLikeIgnoreCase(searchRequest.getKeyword(), searchRequest.getKeyword(), pageable);
        return this.acquirerRepo.findAll(pageable);
    }

    @Override
    public Acquirer getByCodeOrName(String acquirerCode, String fullName) {
        return this.acquirerRepo.findByAcquirerCodeOrFullName(acquirerCode, fullName);
    }

    @Override
    public Acquirer acquirerById(String acquirerId) {
        return this.acquirerRepo.findById(acquirerId).orElse(null);
    }

    @Override
    public void deleteAcquirer(String acquirerId) {
        this.acquirerRepo.deleteById(acquirerId);
    }

    @Override
    public String updateStatus(String acquirerId, String type) {
        Acquirer acquirer = this.acquirerRepo.findById(acquirerId).orElse(null);
        if (acquirer != null) {
            if (type.equalsIgnoreCase("payin")) {
                acquirer.setPayin(!acquirer.isPayin());
                this.acquirerRepo.save(acquirer);
                return acquirer.isPayin() ? "Acquirer Payin Enabled" : "Acquirer Payin Disabled";
            } else if (type.equalsIgnoreCase("payout")) {
                acquirer.setPayout(!acquirer.isPayout());
                this.acquirerRepo.save(acquirer);
                return acquirer.isPayout() ? "Acquirer Payout Enabled" : "Acquirer Payout Disabled";
            } else {
                acquirer.setStatus(!acquirer.isStatus());
                this.acquirerRepo.save(acquirer);
                return acquirer.isStatus() ? "Acquirer Enabled" : "Acquirer Disabled";
            }
        }
        return "Acquirer not found";
    }
}
