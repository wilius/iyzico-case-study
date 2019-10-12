package com.iyzico.challenge.integrator.data.repository;

import com.iyzico.challenge.integrator.data.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Iterable<Product> findAllByStatusNot(Product.Status status);

    Iterable<Product> findAllByStatus(Product.Status status);

}
