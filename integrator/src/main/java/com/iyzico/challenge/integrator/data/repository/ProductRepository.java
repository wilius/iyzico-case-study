package com.iyzico.challenge.integrator.data.repository;

import com.iyzico.challenge.integrator.data.entity.Product;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends CrudRepository<Product, Long> {
}
