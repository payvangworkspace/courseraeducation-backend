package com.pv.couseae.repos;

import com.pv.couseae.entities.Acquirer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AcquirerRepo extends MongoRepository<Acquirer, String> {
    Acquirer findByAcquirerCodeOrFullName(String acquirerCode, String fullName);

    Page<Acquirer> findAllByFullNameLikeIgnoreCaseOrAcquirerCodeLikeIgnoreCase(String fullName,String acquirerCode,Pageable pageable);
}
