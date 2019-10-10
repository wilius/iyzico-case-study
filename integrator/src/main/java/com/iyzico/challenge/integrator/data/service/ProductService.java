package com.iyzico.challenge.integrator.data.service;

import com.iyzico.challenge.integrator.data.entity.Product;
import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.data.repository.ProductRepository;
import com.iyzico.challenge.integrator.exception.ProductNotFoundException;
import com.iyzico.challenge.integrator.service.hazelcast.LockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository repository;
    private final LockService lockService;

    public ProductService(ProductRepository repository,
                          LockService lockService) {
        this.repository = repository;
        this.lockService = lockService;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, noRollbackFor = {
            ProductNotFoundException.class
    })
    public Product getById(long productId) {
        Optional<Product> result = repository.findById(productId);
        if (!result.isPresent()) {
            throw new ProductNotFoundException(String.format("Product with id %s not found", productId));
        }

        Product product = result.get();
        if (Product.Status.DELETED.equals(product.getStatus())) {
            throw new ProductNotFoundException(String.format("Product with id %s not found", productId));
        }

        return product;
    }

    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    public Product create(User user, String name, long stockCount, BigDecimal price) {
        Product product = new Product();
        product.setName(name);
        product.setStatus(Product.Status.UNPUBLISHED);
        product.setStockCount(stockCount);
        product.setUser(user);
        product.setPrice(price);
        product.setAwaitingDeliveryCount(0);
        return repository.save(product);
    }

    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    public Product update(long id, String name, long stockCount, BigDecimal price) {
        return lockService.executeInLock("product-" + id, () -> {
            Product product = getById(id);
            product.setName(name);
            product.setStockCount(stockCount);
            product.setPrice(price);
            return repository.save(product);
        });
    }

}
