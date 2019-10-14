package com.iyzico.challenge.integrator.data.repository;

import com.iyzico.challenge.integrator.data.entity.UserPayment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends CrudRepository<UserPayment, Long> {
}
