package com.iyzico.challenge.integrator.data.service;

import com.iyzico.challenge.integrator.data.entity.Basket;
import com.iyzico.challenge.integrator.data.entity.Product;
import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.data.repository.ProductRepository;
import com.iyzico.challenge.integrator.exception.ProductNotFoundException;
import com.iyzico.challenge.integrator.service.hazelcast.LockService;
import mockit.Deencapsulation;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.StrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;

@RunWith(JMockit.class)
public class ProductServiceTest {

    @Tested
    private ProductService tested;

    @Injectable
    private ProductRepository repository;

    private LockService lockService;

    private ThreadLocal<Lock> threadLocal = new ThreadLocal<>();

    @Before
    public void setup() {
        lockService = createMockLockService();
        tested = new ProductService(repository, lockService);
    }

    @Test(expected = ProductNotFoundException.class)
    public void getById_ProductNotFoundException() {
        long productId = 1;

        Optional<Product> optional = Optional.empty();
        new StrictExpectations() {{
            repository.findById(productId);
            result = optional;
        }};

        tested.getById(productId);
    }

    @Test
    public void getById(@Mocked Product product) {
        long productId = 1;

        Optional<Product> optional = Optional.of(product);
        new StrictExpectations() {{
            repository.findById(productId);
            result = optional;
        }};

        Product result = tested.getById(productId);
        Assert.assertNotNull(result);
        Assert.assertEquals(product, result);
    }

    @Test
    public void getAllItems() {
        Iterable<Product> products = Collections.unmodifiableList(Collections.emptyList());
        new StrictExpectations() {{
            repository.findAll();
            result = products;
        }};

        Iterable<Product> result = tested.getAllItems();
        Assert.assertNotNull(result);
        Assert.assertEquals(products, result);
    }

    @Test
    public void getAllUnpublishedItems() {
        Iterable<Product> products = Collections.unmodifiableList(Collections.emptyList());
        new StrictExpectations() {{
            repository.findAllByStatus(Product.Status.UNPUBLISHED);
            result = products;
        }};

        Iterable<Product> result = tested.getAllUnpublishedItems();
        Assert.assertNotNull(result);
        Assert.assertEquals(products, result);
    }

    @Test(expected = ProductNotFoundException.class)
    public void getPublishedItem_ProductNotFoundException() {
        long productId = 1;

        Optional<Product> optional = Optional.empty();
        new StrictExpectations() {{
            repository.findById(productId);
            result = optional;
        }};

        tested.getById(productId);
    }

    @Test(expected = ProductNotFoundException.class)
    public void getPublishedItem_ProductNotPublished() {
        long productId = 1;

        Product product = new Product();
        product.setStatus(Product.Status.UNPUBLISHED);
        Optional<Product> optional = Optional.of(product);
        new StrictExpectations() {{
            repository.findById(productId);
            result = optional;
        }};

        tested.getPublishedItem(productId);
    }

    @Test
    public void getPublishedItem(@Mocked Product product) {
        long productId = 1;

        Optional<Product> optional = Optional.of(product);
        new NonStrictExpectations() {{
            product.getStatus();
            result = Product.Status.OUT_OF_STOCK;
        }};

        new StrictExpectations() {{
            repository.findById(productId);
            result = optional;
        }};

        Product result = tested.getById(productId);
        Assert.assertNotNull(result);
        Assert.assertEquals(product, result);
    }

    @Test
    public void getAllPublishedItems() {
        Iterable<Product> products = Collections.unmodifiableList(Collections.emptyList());
        new StrictExpectations() {{
            repository.findAllByStatusNot(Product.Status.UNPUBLISHED);
            result = products;
        }};

        Iterable<Product> result = tested.getAllPublishedItems();
        Assert.assertNotNull(result);
        Assert.assertEquals(products, result);
    }

    @Test
    public void create_WithoutDescription(@Mocked User user) {
        String barcode = "barcode";
        String name = "name";
        long stockCount = 10;
        BigDecimal price = BigDecimal.TEN;
        String description = null;

        ProductRepository repository = new MockUp<ProductRepository>() {
            @Mock
            <S> S save(S user1) {
                return user1;
            }

            @Mock
            <S> S saveAndFlush(S user1) {
                return user1;
            }
        }.getMockInstance();

        Deencapsulation.setField(tested, repository);

        Product result = tested.create(user, barcode, name, stockCount, price, description);
        Assert.assertNotNull(result);
        Assert.assertEquals(barcode, result.getBarcode());
        Assert.assertEquals(name, result.getName());
        Assert.assertEquals(stockCount, result.getStockCount());
        Assert.assertEquals(Product.Status.UNPUBLISHED, result.getStatus());
        Assert.assertEquals(price, result.getPrice());
        Assert.assertEquals(user, result.getUser());
        Assert.assertNull(result.getDescription());
    }

    @Test
    public void create(@Mocked User user) {
        String barcode = "barcode";
        String name = "name";
        long stockCount = 10;
        BigDecimal price = BigDecimal.TEN;
        String description = "description";

        ProductRepository repository = new MockUp<ProductRepository>() {
            @Mock
            <S> S save(S user1) {
                return user1;
            }

            @Mock
            <S> S saveAndFlush(S user1) {
                return user1;
            }
        }.getMockInstance();

        Deencapsulation.setField(tested, repository);

        Product result = tested.create(user, barcode, name, stockCount, price, description);
        Assert.assertNotNull(result);
        Assert.assertEquals(barcode, result.getBarcode());
        Assert.assertEquals(name, result.getName());
        Assert.assertEquals(stockCount, result.getStockCount());
        Assert.assertEquals(Product.Status.UNPUBLISHED, result.getStatus());
        Assert.assertEquals(price, result.getPrice());
        Assert.assertEquals(user, result.getUser());
        Assert.assertNotNull(result.getDescription());
        Assert.assertEquals(Product.TABLE_NAME, result.getDescription().getTable());
        Assert.assertEquals(Product.DESCRIPTION_COLUMN_NAME, result.getDescription().getColumnName());
        Assert.assertEquals(description, result.getDescription().getContent());
    }

    @Test
    public void update(@Mocked Lock lock) {
        long id = 1;

        String barcode = "barcode";
        String name = "name";
        long stockCount = 10;
        BigDecimal price = BigDecimal.TEN;
        String description = "description";

        Product product = new Product();
        product.setStatus(Product.Status.IN_STOCK);

        Callable<Basket> callable = () -> null;
        threadLocal.set(lock);
        new StrictExpectations(tested) {{
            lockService.executeInProductLock(id, withInstanceLike(callable));

            tested.getById(id);
            result = product;

            lock.unlock();
        }};

        Product result = tested.update(id, barcode, name, stockCount, price, description);
        Assert.assertNotNull(result);
        Assert.assertEquals(barcode, result.getBarcode());
        Assert.assertEquals(name, result.getName());
        Assert.assertEquals(stockCount, result.getStockCount());
        Assert.assertEquals(Product.Status.IN_STOCK, result.getStatus());
        Assert.assertEquals(price, result.getPrice());
        Assert.assertNotNull(result.getDescription());
        Assert.assertEquals(Product.TABLE_NAME, result.getDescription().getTable());
        Assert.assertEquals(Product.DESCRIPTION_COLUMN_NAME, result.getDescription().getColumnName());
        Assert.assertEquals(description, result.getDescription().getContent());
    }

    @Test
    public void publish(@Mocked Lock lock) {
        long id = 1;

        Product product = new Product();
        product.setStatus(Product.Status.IN_STOCK);
        product.setStockCount(10);

        Callable<Basket> callable = () -> null;
        threadLocal.set(lock);
        new StrictExpectations(tested) {{
            lockService.executeInProductLock(id, withInstanceLike(callable));

            tested.getById(id);
            result = product;

            product.setStatus(Product.Status.IN_STOCK);

            repository.save(product);
            result = product;

            lock.unlock();
        }};

        tested.publish(id);
    }

    @Test
    public void unpublish(@Mocked Lock lock,
                          @Mocked Product product) {
        long id = 1;


        new NonStrictExpectations() {{
            product.getStockCount();
            result = 10;
        }};

        Callable<Basket> callable = () -> null;
        threadLocal.set(lock);
        new StrictExpectations(tested) {{
            lockService.executeInProductLock(id, withInstanceLike(callable));

            tested.getById(id);
            result = product;

            product.setStatus(Product.Status.UNPUBLISHED);

            repository.save(product);
            result = product;

            lock.unlock();
        }};

        tested.unpublish(id);
    }

    private LockService createMockLockService() {
        return new MockUp<LockService>() {
            @Mock
            public <T> T executeInProductLock(long productId, Callable<T> callable) {
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
}