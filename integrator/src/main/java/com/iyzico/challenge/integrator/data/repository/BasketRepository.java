package com.iyzico.challenge.integrator.data.repository;

import com.iyzico.challenge.integrator.data.entity.Basket;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BasketRepository extends CrudRepository<Basket, Long> {
    Basket findFirstByUserIdAndStatus(long userId, Basket.Status status);

    @Query("" +
            " from Basket b " +
            "   left join fetch b.products s " +
            "   join fetch s.product " +
            " where b.userId = :userId " +
            "   and b.status = :status ")
    Basket findFirstByUserIdAndStatusWithBasketContent(@Param("userId") long userId,
                                                       @Param("status") Basket.Status status);
}
