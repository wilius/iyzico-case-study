package com.iyzico.challenge.integrator.data.repository;

import com.iyzico.challenge.integrator.data.entity.UserProfile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends CrudRepository<UserProfile, Long> {
}
