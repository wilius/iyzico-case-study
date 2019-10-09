package com.iyzico.challenge.integrator.data.repository;

import com.iyzico.challenge.integrator.data.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    User findFirstByUsername(String username);
}
