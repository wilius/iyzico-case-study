package com.iyzico.challenge.integrator.data.repository;

import com.iyzico.challenge.integrator.data.entity.BasketProduct;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BasketProductRepository extends CrudRepository<BasketProduct, Long> {
}
