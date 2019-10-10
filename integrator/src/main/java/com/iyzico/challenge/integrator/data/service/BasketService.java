package com.iyzico.challenge.integrator.data.service;

import com.iyzico.challenge.integrator.data.entity.Product;
import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.data.repository.BasketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BasketService {
    private final BasketRepository repository;
    private final ProductService productService;

    public BasketService(BasketRepository repository,
                         ProductService productService) {
        this.repository = repository;
        this.productService = productService;
    }

    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    public void addItem(User user, long productId, int count) {
        Product product = productService.getById(productId);
        if (product.getStockCount() < count) {
            // TODO
            throw new RuntimeException("Over stock count");
        }
    }
}
