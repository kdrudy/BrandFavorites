package com.kyrutech.services;

import com.kyrutech.entities.User;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by kdrudy on 9/26/16.
 */
public interface UserRepository extends CrudRepository<User, Integer> {
    User findFirstByName(String userName);
}
