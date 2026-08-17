package com.pv.couseae.services;

import com.pv.couseae.entities.Acquirer;
import com.pv.couseae.utill.SearchRequest;
import org.springframework.data.domain.Page;

public interface AcquirerService {
    void addNewAcquirer(Acquirer newAcquirer);

    void updateAcquirer(Acquirer acquirer);

    Page<Acquirer> geAllAcquirer(SearchRequest searchRequest);

    Acquirer getByCodeOrName(String acquirerCode, String fullName);

    Acquirer acquirerById(String acquirerId);

    void deleteAcquirer(String acquirerId);

    String updateStatus(String acquirerId, String type);


}
