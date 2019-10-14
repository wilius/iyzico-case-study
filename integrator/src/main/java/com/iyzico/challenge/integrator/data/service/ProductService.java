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

        product = repository.saveAndFlush(product);
        if (StringUtils.isNotEmpty(description)) {
            LongText desc = new LongText();
            desc.setTable(Product.TABLE_NAME);
            desc.setColumnName(Product.DESCRIPTION_COLUMN_NAME);
            desc.setRecordId(String.valueOf(product.getId()));
            desc.setContent(description);

            product.setDescription(desc);
        }

        return product;
    }

    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    public Product update(long id, String barcode, String name, long stockCount, BigDecimal price, String description) {
        return lockService.executeInProductLock(id, () -> {
            Product product = getById(id);
            product.setName(name);
            product.setBarcode(barcode);
            product.setStockCount(stockCount);
            product.setPrice(price);
            if (StringUtils.isNotEmpty(description)) {

                LongText desc = product.getDescription();
                if (desc == null) {
                    desc = new LongText();
                    desc.setTable(Product.TABLE_NAME);
                    desc.setColumnName(Product.DESCRIPTION_COLUMN_NAME);
                    desc.setRecordId(String.valueOf(product.getId()));
                }

                desc.setContent(description);

                product.setDescription(desc);
            }

            return repository.save(product);
        });
    }

    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    public void publish(long id) {
        lockService.executeInProductLock(id, () -> {
            Product product = getById(id);
            updatePublishedProductStatus(product);
            return repository.save(product);
        });
    }

    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    public void unpublish(long id) {
        lockService.executeInProductLock(id, () -> {
            Product product = getById(id);
            product.setStatus(Product.Status.UNPUBLISHED);
            return repository.save(product);
        });
    }

    private void updatePublishedProductStatus(Product product) {
        if (product.getStockCount() > 0) {
            product.setStatus(Product.Status.IN_STOCK);
        } else {
            product.setStatus(Product.Status.OUT_OF_STOCK);
        }
    }
}
