package com.iyzico.challenge.integrator.data.repository;

import com.iyzico.challenge.integrator.data.entity.Payment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends CrudRepository<Payment, Long> {
}
