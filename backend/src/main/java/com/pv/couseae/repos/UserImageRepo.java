package com.pv.couseae.repos;

import com.pv.couseae.entities.UsersImages;
import com.pv.couseae.enums.UserImageTypes;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserImageRepo extends MongoRepository<UsersImages, String> {
    UsersImages findByUserNameAndUserImageType(String userName, UserImageTypes userImageTypes);
}
