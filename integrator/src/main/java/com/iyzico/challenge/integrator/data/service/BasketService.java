package com.iyzico.challenge.integrator.data.service;

import com.iyzico.challenge.integrator.data.entity.Basket;
import com.iyzico.challenge.integrator.data.entity.BasketProduct;
import com.iyzico.challenge.integrator.data.entity.Product;
import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.data.repository.BasketRepository;
import com.iyzico.challenge.integrator.exception.EmptyBasketException;
import com.iyzico.challenge.integrator.exception.InvalidBasketStatusException;
import com.iyzico.challenge.integrator.exception.StockNotEnoughException;
import com.iyzico.challenge.integrator.service.hazelcast.BLock;
import com.iyzico.challenge.integrator.service.hazelcast.LockService;
import com.iyzico.challenge.integrator.service.hazelcast.exception.CannotHoldTheLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class BasketService {
    private final Logger log = LoggerFactory.getLogger(BasketService.class);
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
    public void addItem(User user, long productId, int count) {
        lockService.executeInBasketLock(user, () -> {
            Product product = productService.getPublishedItem(productId);
            if (!Product.Status.IN_STOCK.equals(product.getStatus())) {
                throw new StockNotEnoughException("No product left to sell");
            }

            if (product.getStockCount() < count) {
                throw new StockNotEnoughException("Stock count is not enough");
            }

            Basket basket = getUserBasket(user);
            for (BasketProduct basketProduct : basket.getProducts()) {
                if (basketProduct.getProductId() == productId) {
                    basketProduct.setCount(basketProduct.getCount() + count);
                    return repository.save(basket);
                }
            }

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
    public void deleteItem(User user, long basketProductId) {
        lockService.executeInBasketLock(user, () -> {
            Basket basket = getUserBasket(user);
            Iterator<BasketProduct> iterator = basket.getProducts().iterator();
            while (iterator.hasNext()) {
                BasketProduct next = iterator.next();
                if (next.getId() == basketProductId) {
                    iterator.remove();
                    return repository.save(basket);
                }
            }

            return basket;
        });
    }

    @Transactional(propagation = Propagation.NEVER)
    public Basket decreaseStocks(User user, Basket basket) {
        return updateStock(user, basket, Basket.Status.STOCK_APPLIED);
    }

    @Transactional(propagation = Propagation.NEVER)
    public Basket rollbackStocks(User user, Basket basket) {
        return updateStock(user, basket, Basket.Status.ACTIVE);
    }

    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    void complete(Basket basket) {
        basket.setStatus(Basket.Status.COMPLETED);
        repository.save(basket);
    }

    public Basket getUserBasket(User user) {
        List<Basket.Status> statuses = Arrays.asList(Basket.Status.ACTIVE, Basket.Status.STOCK_APPLIED);
        Basket basket = repository.findFirstByUserIdAndStatusWithBasketContent(user.getId(), statuses);

        if (basket != null) {
            return basket;
        }

        return lockService.executeInBasketLock(user, () -> {
            Basket innerCheck = repository.findFirstByUserIdAndStatusWithBasketContent(user.getId(), statuses);

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

    private Basket updateStock(User user, Basket basket, Basket.Status status) {
        List<BLock> locks = new ArrayList<>();

        int multiplier = validateBasketAndGetMultiplier(basket, status);

        try {
            lockBasketProducts(basket, locks);
        } catch (CannotHoldTheLockException e) {
            unlockAll(locks);
            throw e;
        }

        try {
            return requireNewTransactionTemplate.execute(x -> {
                Basket innerBasket = getUserBasket(user);
                for (BasketProduct basketProduct : innerBasket.getProducts()) {
                    Product product = basketProduct.getProduct();
                    long count = product.getStockCount() + (multiplier * basketProduct.getCount());
                    if (multiplier < 0 && count < 0) {
                        throw new StockNotEnoughException(String.format("Stock not enough for product %s", product.getId()));
                    }

                    product.setStockCount(count);
                }

                innerBasket.setStatus(status);
                return repository.save(innerBasket);
            });
        } finally {
            unlockAll(locks);
        }
    }

    private int validateBasketAndGetMultiplier(Basket basket, Basket.Status status) {

        if (CollectionUtils.isEmpty(basket.getProducts())) {
            throw new EmptyBasketException("Basket has no item to purchase");
        }

        if (Basket.Status.STOCK_APPLIED.equals(status)) {
            if (!Basket.Status.ACTIVE.equals(basket.getStatus())) {
                throw new InvalidBasketStatusException(String.format("Invalid basket status %s", basket.getStatus()));
            }

            return -1;
        }

        if (Basket.Status.ACTIVE.equals(status)) {
            if (!Basket.Status.STOCK_APPLIED.equals(basket.getStatus())) {
                throw new InvalidBasketStatusException(String.format("Invalid basket status %s", basket.getStatus()));
            }

            return 1;
        }

        throw new InvalidBasketStatusException(String.format("Invalid basket status %s", status));


    }

    private void lockBasketProducts(Basket basket, List<BLock> locks) throws CannotHoldTheLockException {
        List<Long> productIds = basket.getProducts()
                .stream()
                .map(BasketProduct::getProductId)
                .sorted()
                .collect(Collectors.toList());

        for (Long productId : productIds) {
            log.trace("Trying to get lock for productId {}", productId);
            BLock lock = lockService.getProductLock(productId);
            try {
                if (!lock.tryLock(30, TimeUnit.SECONDS)) {
                    log.trace("Lock could not hold for productId {}", productId);
                    throw new CannotHoldTheLockException(String.format("Lock for product %s cannot hold in time interval", productId));
                }
            } catch (InterruptedException e) {
                log.trace("Unexpected exception while trying to get lock for productId {}", productId, e);
                throw new CannotHoldTheLockException(e);
            }

            log.trace("Lock held successfully for productId {}", productId);
            locks.add(lock);
        }

        log.trace("All product locks held for basket {}", basket.getId());
    }

    private void unlockAll(List<BLock> locks) {
        for (BLock lock : locks) {
            for (int i = 0; i < 10; i++) {
                try {
                    lock.unlock();
                    log.trace("Lock {} successfully released", lock.getName());
                    break;
                } catch (Throwable e) {

                    if (i == 9) {
                        log.warn("Cannot release the lock after 10 retries. lockKey: " + lock.getName(), e);
                    } else {
                        log.trace("Unexpected exception during unlock of key " + lock.getName(), e);
                    }
                }
            }
        }
    }
}
