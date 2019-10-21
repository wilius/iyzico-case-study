package com.iyzico.challenge.integrator.data.service;

import com.iyzico.challenge.integrator.data.entity.Basket;
import com.iyzico.challenge.integrator.data.entity.BasketProduct;
import com.iyzico.challenge.integrator.data.entity.Product;
import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.data.repository.BasketRepository;
import com.iyzico.challenge.integrator.exception.EmptyBasketException;
import com.iyzico.challenge.integrator.exception.InvalidBasketStatusException;
import com.iyzico.challenge.integrator.exception.ProductNotFoundException;
import com.iyzico.challenge.integrator.exception.StockNotEnoughException;
import com.iyzico.challenge.integrator.service.hazelcast.BLock;
import com.iyzico.challenge.integrator.service.hazelcast.LockService;
import com.iyzico.challenge.integrator.service.hazelcast.exception.CannotHoldTheLockException;
import mockit.Deencapsulation;
import mockit.Delegate;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.StrictExpectations;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@RunWith(JMockit.class)
public class BasketServiceTest {
    private BasketService tested;

    @Injectable
    private BasketRepository repository;

    @Injectable
    private ProductService productService;

    @Injectable
    private PlatformTransactionManager manager;

    private TransactionTemplate requireNewTransactionTemplate;

    private LockService lockService;

    private ThreadLocal<Lock> threadLocal = new ThreadLocal<>();

    @Before
    public void setup() {
        lockService = createMockLockService();
        tested = new BasketService(repository, productService, manager, lockService);
        requireNewTransactionTemplate = new MockUp<TransactionTemplate>() {

            @Mock
            public <T> T execute(TransactionCallback<T> action) {
                return action.doInTransaction(null);
            }
        }.getMockInstance();

        Deencapsulation.setField(tested, requireNewTransactionTemplate);
    }

    @Test
    public void translation_template(@Mocked PlatformTransactionManager manager) {
        BasketService tested = new BasketService(repository, productService, manager, lockService);
        Assert.assertEquals(repository, Deencapsulation.getField(tested, "repository"));
        Assert.assertEquals(productService, Deencapsulation.getField(tested, "productService"));
        Assert.assertEquals(lockService, Deencapsulation.getField(tested, "lockService"));
        TransactionTemplate requireNewTransactionTemplate = Deencapsulation.getField(tested, "requireNewTransactionTemplate");
        Assert.assertEquals(requireNewTransactionTemplate.getPropagationBehavior(), TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Test(expected = ProductNotFoundException.class)
    public void addItem_ProductNotFoundException(@Mocked User user,
                                                 @Mocked Lock lock) {
        long productId = 1;
        int count = 2;

        Callable<Basket> callable = () -> null;
        threadLocal.set(lock);

        new StrictExpectations() {{
            lockService.executeInBasketLock(user, withInstanceLike(callable));

            productService.getPublishedItem(productId);
            result = new ProductNotFoundException("test");

            lock.unlock();
        }};

        tested.addItem(user, productId, count);
    }

    @Test(expected = StockNotEnoughException.class)
    public void addItem_StatusIsOutOfStock(@Mocked User user,
                                           @Mocked Product product,
                                           @Mocked Lock lock) {
        long productId = 1;
        int count = 2;

        Callable<Basket> callable = () -> null;
        threadLocal.set(lock);

        new StrictExpectations() {{
            lockService.executeInBasketLock(user, withInstanceLike(callable));

            productService.getPublishedItem(productId);
            result = product;

            product.getStatus();
            result = Product.Status.OUT_OF_STOCK;

            lock.unlock();
        }};

        tested.addItem(user, productId, count);
    }

    @Test(expected = StockNotEnoughException.class)
    public void addItem_StockCountNotEnough(@Mocked User user,
                                            @Mocked Product product,
                                            @Mocked Lock lock) {
        long productId = 1;
        int count = 2;

        Callable<Basket> callable = () -> null;
        threadLocal.set(lock);

        new StrictExpectations() {{
            lockService.executeInBasketLock(user, withInstanceLike(callable));

            productService.getPublishedItem(productId);
            result = product;

            product.getStatus();
            result = Product.Status.IN_STOCK;

            product.getStockCount();
            result = count - 1;

            lock.unlock();
        }};

        tested.addItem(user, productId, count);
    }

    @Test
    public void addItem_ProductAlreadyExistsInBasket(@Mocked User user,
                                                     @Mocked Product product,
                                                     @Mocked Basket basket,
                                                     @Mocked Lock lock) {
        long productId = 1;
        int count = 2;
        BasketProduct basketProduct = new BasketProduct();
        basketProduct.setCount(count);
        basketProduct.setProduct(product);
        basketProduct.setProductId(productId);
        basketProduct.setBasket(basket);

        Callable<Basket> callable = () -> null;
        threadLocal.set(lock);

        Set<BasketProduct> products = Collections.singleton(basketProduct);
        new StrictExpectations(tested) {{
            lockService.executeInBasketLock(user, withInstanceLike(callable));

            productService.getPublishedItem(productId);
            result = product;

            product.getStatus();
            result = Product.Status.IN_STOCK;

            product.getStockCount();
            result = count;

            tested.getUserBasket(user);
            result = basket;

            basket.getProducts();
            result = products;

            lock.unlock();
        }};

        tested.addItem(user, productId, count);
        Assert.assertEquals(count * 2, basketProduct.getCount());
    }

    @Test
    public void addItem(@Mocked User user,
                        @Mocked Product product,
                        @Mocked Lock lock) {
        long productId = 1;
        int count = 2;

        Set<BasketProduct> products = new HashSet<>();
        Basket basket = new Basket();
        basket.setProducts(products);

        LocalDateTime now = LocalDateTime.now();

        Callable<Basket> callable = () -> null;
        threadLocal.set(lock);
        new StrictExpectations(tested, LocalDateTime.now()) {{
            lockService.executeInBasketLock(user, withInstanceLike(callable));

            productService.getPublishedItem(productId);
            result = product;

            product.getStatus();
            result = Product.Status.IN_STOCK;

            product.getStockCount();
            result = count;

            tested.getUserBasket(user);
            result = basket;

            LocalDateTime.now();
            result = now;

            repository.save(basket);
            result = basket;

            lock.unlock();
        }};

        tested.addItem(user, productId, count);
        Assert.assertEquals(1, products.size());
        BasketProduct basketProduct = products.iterator().next();
        Assert.assertEquals(basket, basketProduct.getBasket());
        Assert.assertEquals(product, basketProduct.getProduct());
        Assert.assertEquals(count, basketProduct.getCount());
        Assert.assertEquals(now, basketProduct.getCreateTime());
    }

    @Test
    public void deleteItem_UnableToFindItemInBasket(@Mocked User user,
                                                    @Mocked Lock lock,
                                                    @Mocked Basket basket) {
        long basketProductId = 1;
        Set<BasketProduct> products = new HashSet<>();
        BasketProduct product = new BasketProduct();
        product.setId(basketProductId + 1);
        products.add(product);

        Callable<Basket> callable = () -> null;
        threadLocal.set(lock);
        new StrictExpectations() {{
            lockService.executeInBasketLock(user, withInstanceLike(callable));

            basket.getProducts();
            result = products;
        }};

        tested.deleteItem(user, basketProductId);
        Assert.assertEquals(1, products.size());
    }

    @Test
    public void deleteItem(@Mocked User user,
                           @Mocked Lock lock,
                           @Mocked Basket basket) {
        long basketProductId = 1;
        Set<BasketProduct> products = new HashSet<>();
        BasketProduct product = new BasketProduct();
        product.setId(basketProductId);
        products.add(product);

        Callable<Basket> callable = () -> null;
        threadLocal.set(lock);
        new StrictExpectations() {{
            lockService.executeInBasketLock(user, withInstanceLike(callable));

            basket.getProducts();
            result = products;
        }};

        tested.deleteItem(user, basketProductId);
        Assert.assertTrue(products.isEmpty());
    }

    @Test(expected = EmptyBasketException.class)
    public void decreaseStocks_NullProducts(@Mocked User user,
                                            @Mocked Basket basket) {

        new StrictExpectations() {{
            basket.getProducts();
            result = null;
        }};

        tested.decreaseStocks(user, basket);
    }

    @Test(expected = InvalidBasketStatusException.class)
    public void decreaseStocks_StockAlreadyApplied(@Mocked User user) {

        Set<BasketProduct> products = new HashSet<>();
        BasketProduct product = new BasketProduct();
        products.add(product);

        Basket basket = new Basket();
        basket.setStatus(Basket.Status.STOCK_APPLIED);
        basket.setProducts(products);
        tested.decreaseStocks(user, basket);
    }

    @Test(expected = InvalidBasketStatusException.class)
    public void decreaseStocks_Completed(@Mocked User user) {

        Set<BasketProduct> products = new HashSet<>();
        BasketProduct product = new BasketProduct();
        products.add(product);

        Basket basket = new Basket();
        basket.setStatus(Basket.Status.COMPLETED);
        basket.setProducts(products);
        tested.decreaseStocks(user, basket);
    }

    @Test(expected = CannotHoldTheLockException.class)
    public void decreaseStocks_CannotHoldTheLock(@Mocked User user,
                                                 @Mocked BLock lock1,
                                                 @Mocked BLock lock2) throws InterruptedException {

        long productId1 = 10L;
        long productId2 = 5L;
        Set<BasketProduct> products = new TreeSet<>();
        BasketProduct basketProduct = new BasketProduct();
        basketProduct.setCount(5);
        basketProduct.setProductId(productId1);
        basketProduct.setProduct(createProduct(productId1, 10, BigDecimal.TEN));
        products.add(basketProduct);

        basketProduct = new BasketProduct();
        basketProduct.setCount(5);
        basketProduct.setProductId(productId2);
        basketProduct.setProduct(createProduct(productId2, 10, BigDecimal.TEN));
        products.add(basketProduct);

        Basket basket = new Basket();
        basket.setStatus(Basket.Status.ACTIVE);
        basket.setProducts(products);

        new NonStrictExpectations() {{
            lock1.getName();
            result = "lock1";

            lock2.getName();
            result = "lock2";
        }};

        new StrictExpectations(lockService) {{
            lockService.getProductLock(productId2);
            result = lock1;

            lock1.tryLock(30, TimeUnit.SECONDS);
            result = true;

            lockService.getProductLock(productId1);
            result = lock2;

            lock2.tryLock(30, TimeUnit.SECONDS);
            result = false;

            lock1.unlock();
        }};

        tested.decreaseStocks(user, basket);
    }

    @Test(expected = StockNotEnoughException.class)
    public void decreaseStocks_StockNotEnoughException(@Mocked User user,
                                                       @Mocked BLock lock1,
                                                       @Mocked BLock lock2) throws InterruptedException {

        long productId1 = 10L;
        long productId2 = 5L;
        Set<BasketProduct> products = new TreeSet<>();
        BasketProduct basketProduct = new BasketProduct();
        basketProduct.setCount(15);
        basketProduct.setProductId(productId1);
        basketProduct.setProduct(createProduct(productId1, 10, BigDecimal.TEN));
        products.add(basketProduct);

        basketProduct = new BasketProduct();
        basketProduct.setCount(5);
        basketProduct.setProductId(productId2);
        basketProduct.setProduct(createProduct(productId2, 10, BigDecimal.TEN));
        products.add(basketProduct);

        Basket basket = new Basket();
        basket.setStatus(Basket.Status.ACTIVE);
        basket.setProducts(products);

        new NonStrictExpectations() {{
            lock1.getName();
            result = "lock1";

            lock2.getName();
            result = "lock2";
        }};

        TransactionCallback<Basket> transactionCallback = status -> null;
        new StrictExpectations(lockService, tested, basket) {{
            lockService.getProductLock(productId2);
            result = lock1;

            lock1.tryLock(30, TimeUnit.SECONDS);
            result = true;

            lockService.getProductLock(productId1);
            result = lock2;

            lock2.tryLock(30, TimeUnit.SECONDS);
            result = true;

            requireNewTransactionTemplate.execute(withInstanceLike(transactionCallback));

            lock1.unlock();

            lock2.unlock();
        }};

        tested.decreaseStocks(user, basket);
    }

    @Test
    public void decreaseStocks(@Mocked User user,
                               @Mocked BLock lock1,
                               @Mocked BLock lock2) throws InterruptedException {

        long userId = Long.MAX_VALUE;

        long productId1 = 10L;
        long productId2 = 5L;
        Set<BasketProduct> products = new TreeSet<>();
        BasketProduct basketProduct1 = new BasketProduct();
        basketProduct1.setCount(10);
        basketProduct1.setProductId(productId1);
        basketProduct1.setProduct(createProduct(productId1, 10, BigDecimal.TEN));
        products.add(basketProduct1);

        BasketProduct basketProduct2 = new BasketProduct();
        basketProduct2.setCount(5);
        basketProduct2.setProductId(productId2);
        basketProduct2.setProduct(createProduct(productId2, 10, BigDecimal.TEN));
        products.add(basketProduct2);

        Basket basket = new Basket();
        basket.setStatus(Basket.Status.ACTIVE);
        basket.setProducts(products);

        TransactionCallback<Basket> transactionCallback = status -> null;
        new NonStrictExpectations() {{
            user.getId();
            result = userId;

            basket.getProducts();
            result = products;

            lock1.getName();
            result = "lock1";

            lock2.getName();
            result = "lock2";
        }};

        new StrictExpectations(lockService, basket) {{
            lockService.getProductLock(productId2);
            result = lock1;

            lock1.tryLock(30, TimeUnit.SECONDS);
            result = true;

            lockService.getProductLock(productId1);
            result = lock2;

            lock2.tryLock(30, TimeUnit.SECONDS);
            result = true;

            requireNewTransactionTemplate.execute(withInstanceLike(transactionCallback));

            lock1.unlock();

            lock2.unlock();
        }};

        Basket result = tested.decreaseStocks(user, basket);
        Assert.assertEquals(Basket.Status.STOCK_APPLIED, result.getStatus());
        Assert.assertEquals(0, basketProduct1.getProduct().getStockCount());
        Assert.assertEquals(5, basketProduct2.getProduct().getStockCount());
    }


    @Test(expected = EmptyBasketException.class)
    public void rollbackStocks_NullProducts(@Mocked User user,
                                            @Mocked Basket basket) {

        new StrictExpectations() {{
            basket.getProducts();
            result = null;
        }};

        tested.rollbackStocks(user, basket);
    }

    @Test(expected = InvalidBasketStatusException.class)
    public void rollbackStocks_AlreadyActive(@Mocked User user) {

        Set<BasketProduct> products = new HashSet<>();
        BasketProduct product = new BasketProduct();
        products.add(product);

        Basket basket = new Basket();
        basket.setStatus(Basket.Status.ACTIVE);
        basket.setProducts(products);
        tested.rollbackStocks(user, basket);
    }

    @Test(expected = InvalidBasketStatusException.class)
    public void rollbackStocks_Completed(@Mocked User user) {

        Set<BasketProduct> products = new HashSet<>();
        BasketProduct product = new BasketProduct();
        products.add(product);

        Basket basket = new Basket();
        basket.setStatus(Basket.Status.COMPLETED);
        basket.setProducts(products);
        tested.rollbackStocks(user, basket);
    }

    @Test(expected = CannotHoldTheLockException.class)
    public void rollbackStocks_CannotHoldTheLock(@Mocked User user,
                                                 @Mocked BLock lock1,
                                                 @Mocked BLock lock2) throws InterruptedException {

        long productId1 = 10L;
        long productId2 = 5L;
        Set<BasketProduct> products = new TreeSet<>();
        BasketProduct basketProduct = new BasketProduct();
        basketProduct.setCount(5);
        basketProduct.setProductId(productId1);
        basketProduct.setProduct(createProduct(productId1, 10, BigDecimal.TEN));
        products.add(basketProduct);

        basketProduct = new BasketProduct();
        basketProduct.setCount(5);
        basketProduct.setProductId(productId2);
        basketProduct.setProduct(createProduct(productId2, 10, BigDecimal.TEN));
        products.add(basketProduct);

        Basket basket = new Basket();
        basket.setStatus(Basket.Status.ACTIVE);
        basket.setProducts(products);

        new NonStrictExpectations() {{
            lock1.getName();
            result = "lock1";

            lock2.getName();
            result = "lock2";
        }};

        new StrictExpectations(lockService) {{
            lockService.getProductLock(productId2);
            result = lock1;

            lock1.tryLock(30, TimeUnit.SECONDS);
            result = true;

            lockService.getProductLock(productId1);
            result = lock2;

            lock2.tryLock(30, TimeUnit.SECONDS);
            result = false;

            lock1.unlock();
        }};

        tested.decreaseStocks(user, basket);
    }

    @Test
    public void rollbackStocks(@Mocked User user,
                               @Mocked BLock lock1,
                               @Mocked BLock lock2) throws InterruptedException {

        long userId = Long.MAX_VALUE;

        long productId1 = 10L;
        long productId2 = 5L;
        Set<BasketProduct> products = new TreeSet<>();
        BasketProduct basketProduct1 = new BasketProduct();
        basketProduct1.setCount(10);
        basketProduct1.setProductId(productId1);
        basketProduct1.setProduct(createProduct(productId1, 10, BigDecimal.TEN));
        products.add(basketProduct1);

        BasketProduct basketProduct2 = new BasketProduct();
        basketProduct2.setCount(5);
        basketProduct2.setProductId(productId2);
        basketProduct2.setProduct(createProduct(productId2, 10, BigDecimal.TEN));
        products.add(basketProduct2);

        Basket basket = new Basket();
        basket.setStatus(Basket.Status.STOCK_APPLIED);
        basket.setProducts(products);

        TransactionCallback<Basket> transactionCallback = status -> null;
        new NonStrictExpectations() {{
            user.getId();
            result = userId;

            basket.getProducts();
            result = products;

            lock1.getName();
            result = "lock1";

            lock2.getName();
            result = "lock2";
        }};

        new StrictExpectations(lockService, basket) {{
            lockService.getProductLock(productId2);
            result = lock1;

            lock1.tryLock(30, TimeUnit.SECONDS);
            result = true;

            lockService.getProductLock(productId1);
            result = lock2;

            lock2.tryLock(30, TimeUnit.SECONDS);
            result = true;

            requireNewTransactionTemplate.execute(withInstanceLike(transactionCallback));

            lock1.unlock();

            lock2.unlock();
        }};

        Basket result = tested.rollbackStocks(user, basket);
        Assert.assertEquals(Basket.Status.ACTIVE, result.getStatus());
        Assert.assertEquals(20, basketProduct1.getProduct().getStockCount());
        Assert.assertEquals(15, basketProduct2.getProduct().getStockCount());
    }


    @Test
    public void getByUser_WithoutNewBasketCreation(@Mocked User user,
                                                   @Mocked Basket basket) {
        long userId = Long.MAX_VALUE;

        new StrictExpectations() {{
            user.getId();
            result = userId;

            repository.findFirstByUserIdAndStatusWithBasketContent(userId, with(new Delegate<List<Basket.Status>>() {
                public boolean matches(List<Basket.Status> statuses) {
                    return statuses != null &&
                            statuses.contains(Basket.Status.ACTIVE) &&
                            statuses.contains(Basket.Status.STOCK_APPLIED);
                }
            }));
            result = basket;
        }};

        Basket result = tested.getUserBasket(user);
        Assert.assertEquals(basket, result);
    }


    @Test
    public void getByUser_NeedToGetLockButAnotherProcessAlreadyCreatedEntity(@Mocked User user,
                                                                             @Mocked Lock lock,
                                                                             @Mocked Basket basket) {
        long userId = Long.MAX_VALUE;

        Callable<Basket> callable = () -> null;
        threadLocal.set(lock);
        new StrictExpectations() {{
            user.getId();
            result = userId;

            repository.findFirstByUserIdAndStatusWithBasketContent(userId, with(new Delegate<List<Basket.Status>>() {
                public boolean matches(List<Basket.Status> statuses) {
                    return statuses != null &&
                            statuses.contains(Basket.Status.ACTIVE) &&
                            statuses.contains(Basket.Status.STOCK_APPLIED);
                }
            }));
            result = null;

            lockService.executeInBasketLock(user, withInstanceLike(callable));

            user.getId();
            result = userId;

            repository.findFirstByUserIdAndStatusWithBasketContent(userId, with(new Delegate<List<Basket.Status>>() {
                public boolean matches(List<Basket.Status> statuses) {
                    return statuses != null &&
                            statuses.contains(Basket.Status.ACTIVE) &&
                            statuses.contains(Basket.Status.STOCK_APPLIED);
                }
            }));
            result = basket;

            lock.unlock();

        }};

        Basket result = tested.getUserBasket(user);
        Assert.assertEquals(basket, result);
    }

    @Test
    public void getByUser_CreatedNewBasket(@Mocked User user,
                                           @Mocked Lock lock,
                                           @Mocked Basket basket) {

        long userId = Long.MAX_VALUE;
        Callable<Basket> callable = () -> null;
        TransactionCallback<Basket> transactionCallback = status -> null;

        threadLocal.set(lock);
        new StrictExpectations() {{
            user.getId();
            result = userId;

            repository.findFirstByUserIdAndStatusWithBasketContent(userId, with(new Delegate<List<Basket.Status>>() {
                public boolean matches(List<Basket.Status> statuses) {
                    return statuses != null &&
                            statuses.contains(Basket.Status.ACTIVE) &&
                            statuses.contains(Basket.Status.STOCK_APPLIED);
                }
            }));
            result = null;

            lockService.executeInBasketLock(user, withInstanceLike(callable));

            user.getId();
            result = userId;

            repository.findFirstByUserIdAndStatusWithBasketContent(userId, with(new Delegate<List<Basket.Status>>() {
                public boolean matches(List<Basket.Status> statuses) {
                    return statuses != null &&
                            statuses.contains(Basket.Status.ACTIVE) &&
                            statuses.contains(Basket.Status.STOCK_APPLIED);
                }
            }));
            result = null;

            requireNewTransactionTemplate.execute(withInstanceLike(transactionCallback));

            new Basket();
            result = basket;

            basket.setStatus(Basket.Status.ACTIVE);
            basket.setUser(user);
            repository.save(basket);

            lock.unlock();

        }};

        Basket result = tested.getUserBasket(user);
        Assert.assertNotNull(result);
        Assert.assertEquals(basket, result);
    }

    @Test
    public void complete(@Mocked Basket basket) {
        new StrictExpectations() {{
            basket.setStatus(Basket.Status.COMPLETED);

            repository.save(basket);
        }};

        tested.complete(basket);
    }

    private LockService createMockLockService() {
        return new MockUp<LockService>() {
            @Mock
            public BLock getProductLock(Long productId) {
                return null;
            }

            @Mock
            public <T> T executeInBasketLock(User user, Callable<T> callable) {
                try {
                    return callable.call();
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    threadLocal.get().unlock();
                }
            }
        }.getMockInstance();
    }

    private Product createProduct(long id, long stockCount, BigDecimal price) {
        Product product = new Product();
        product.setId(id);
        product.setStockCount(stockCount);
        product.setStatus(stockCount > 0 ? Product.Status.IN_STOCK : Product.Status.OUT_OF_STOCK);
        product.setPrice(price);
        return product;
    }
}