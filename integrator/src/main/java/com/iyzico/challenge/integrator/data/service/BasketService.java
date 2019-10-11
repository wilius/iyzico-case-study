package com.iyzico.challenge.integrator.data.service;

import com.iyzico.challenge.integrator.data.entity.Basket;
import com.iyzico.challenge.integrator.data.entity.BasketProduct;
import com.iyzico.challenge.integrator.data.entity.Product;
import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.data.repository.BasketRepository;
import com.iyzico.challenge.integrator.service.hazelcast.LockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.concurrent.Callable;

@Service
public class BasketService {
    private final BasketRepository repository;
    private final ProductService productService;
    private final TransactionTemplate requireNewTransactionTemplate;
    private final LockService lockService;

    public BasketService(BasketRepository repository,
                         ProductService productService,
                         PlatformTransactionManager transactionManager,
                         LockService lockService) {
        this.repository = repository;
        this.productService = productService;
        this.lockService = lockService;

        requireNewTransactionTemplate = new TransactionTemplate(transactionManager);
        requireNewTransactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    public Basket addItem(User user, long productId, int count) {
        return executeInBasketEditLock(user, () -> {
            Product product = productService.getById(productId);
            if (product.getStockCount() < count) {
                // TODO
                throw new RuntimeException("Over stock count");
            }

            Basket basket = getUserBasket(user);
            BasketProduct basketProduct = new BasketProduct();
            basketProduct.setBasket(basket);
            basketProduct.setProduct(product);
            basketProduct.setCount(count);
            basketProduct.setCreateTime(LocalDateTime.now());
            basket.getProducts().add(basketProduct);
            return repository.save(basket);
        });
    }

    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    public Basket deleteItem(User user, long basketProductId) {
        return executeInBasketEditLock(user, () -> {
            Basket basket = getUserBasket(user);
            Iterator<BasketProduct> iterator = basket.getProducts().iterator();
            while (iterator.hasNext()) {
                BasketProduct next = iterator.next();
                if (next.getId() == basketProductId) {
                    iterator.remove();
                    break;
                }
            }

            return repository.save(basket);
        });
    }

    public Basket getByUser(User user) {
        return getUserBasket(user);
    }

    private Basket getUserBasket(User user) {
        Basket basket = repository.findFirstByUserIdAndStatus(user.getId(), Basket.Status.ACTIVE);
        if (basket != null) {
            return basket;
        }

        return lockService.executeInLock("basket-creation-lock:" + user.getId(), () -> {
            Basket innerCheck = repository.findFirstByUserIdAndStatus(user.getId(), Basket.Status.ACTIVE);
            if (innerCheck != null) {
                return innerCheck;
            }

            return requireNewTransactionTemplate.execute(status -> {
                Basket created = new Basket();
                created.setStatus(Basket.Status.ACTIVE);
                created.setUser(user);
                return repository.save(created);
            });
        });
    }

    private <T> T executeInBasketEditLock(User user, Callable<T> callable) {
        return lockService.executeInLock("basket-edit-lock:" + user.getId(), callable);
    }
}
