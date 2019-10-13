package com.iyzico.challenge.integrator.data.service;

import com.iyzico.challenge.integrator.data.entity.LongText;
import com.iyzico.challenge.integrator.data.entity.Product;
import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.data.repository.ProductRepository;
import com.iyzico.challenge.integrator.exception.ProductNotFoundException;
import com.iyzico.challenge.integrator.service.hazelcast.LockService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.Callable;

@Service
public class ProductService {
    private final ProductRepository repository;
    private final LongTextService longTextService;
    private final LockService lockService;

    public ProductService(ProductRepository repository,
                          LongTextService longTextService,
                          LockService lockService) {
        this.repository = repository;
        this.longTextService = longTextService;
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

        return result.get();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Iterable<Product> getAllItems() {
        return repository.findAll();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Iterable<Product> getAllUnpublishedItems() {
        return repository.findAllByStatus(Product.Status.UNPUBLISHED);
    }

    @Transactional(propagation = Propagation.SUPPORTS, noRollbackFor = Throwable.class, readOnly = true)
    public Product getPublishedItem(long id) {
        Product product = getById(id);
        if (Product.Status.UNPUBLISHED.equals(product.getStatus())) {
            throw new ProductNotFoundException(String.format("Product with id %s not found", id));
        }

        return product;
    }

    @Transactional(propagation = Propagation.SUPPORTS, noRollbackFor = Throwable.class, readOnly = true)
    public Iterable<Product> getAllPublishedItems() {
        return repository.findAllByStatusNot(Product.Status.UNPUBLISHED);
    }

    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    public Product create(User user, String barcode, String name, long stockCount, BigDecimal price, String description) {
        Product product = new Product();
        product.setName(name);
        product.setBarcode(barcode);
        product.setStatus(Product.Status.UNPUBLISHED);
        product.setStockCount(stockCount);
        product.setUser(user);
        product.setPrice(price);
        product.setAwaitingDeliveryCount(0);

        product = repository.saveAndFlush(product);
        if (StringUtils.isNotEmpty(description)) {
            LongText desc = longTextService.create(
                    Product.TABLE_NAME,
                    Product.DESCRIPTION_COLUMN_NAME,
                    String.valueOf(product.getId()),
                    description);

            product.setDescription(desc);
        }

        return product;
    }

    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    public Product update(long id, String barcode, String name, long stockCount, BigDecimal price, String description) {
        return execute(id, () -> {
            Product product = getById(id);
            if (stockCount < product.getAwaitingDeliveryCount()) {
                // TODO convert it to a known error
                throw new RuntimeException();
            }

            product.setName(name);
            product.setBarcode(barcode);
            product.setStockCount(stockCount);
            product.setPrice(price);
            return repository.save(product);
        });
    }

    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    public Product publish(long id) {
        return execute(id, () -> {
            Product product = getById(id);
            updatePublishedProductStatus(product);
            return repository.save(product);
        });
    }

    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    public Product unpublish(long id) {
        return execute(id, () -> {
            Product product = getById(id);
            product.setStatus(Product.Status.UNPUBLISHED);
            return repository.save(product);
        });
    }

    private <T> T execute(long productId, Callable<T> callable) {
        return lockService.executeInLock("product-" + productId, callable);
    }

    private void updatePublishedProductStatus(Product product) {
        if (product.hasItemToSell()) {
            product.setStatus(Product.Status.IN_STOCK);
        } else {
            product.setStatus(Product.Status.OUT_OF_STOCK);
        }
    }
}
