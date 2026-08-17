package com.pv.couseae.repos;

import com.pv.couseae.entities.Documents;
import com.pv.couseae.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DocumentsRepo extends MongoRepository<Documents, String> {
    Documents findByDocumentTypeAndUser(String documentType, User user);

    List<Documents> findAllByUser(User user);
}
