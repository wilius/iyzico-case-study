package com.iyzico.challenge.integrator.data.repository;

import com.iyzico.challenge.integrator.data.entity.UserOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserOrderRepository extends JpaRepository<UserOrder, Long> {
}
