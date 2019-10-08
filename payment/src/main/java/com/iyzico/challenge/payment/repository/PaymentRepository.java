package com.iyzico.challenge.payment.repository;

import com.iyzico.challenge.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
