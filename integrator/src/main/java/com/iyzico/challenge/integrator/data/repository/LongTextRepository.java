package com.iyzico.challenge.integrator.data.repository;

import com.iyzico.challenge.integrator.data.entity.LongText;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LongTextRepository extends JpaRepository<LongText, Long> {
}
