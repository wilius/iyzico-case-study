package com.iyzico.challenge.integrator.mapper;

import com.iyzico.challenge.integrator.data.entity.Basket;
import com.iyzico.challenge.integrator.data.entity.BasketProduct;
import com.iyzico.challenge.integrator.data.entity.Product;
import com.iyzico.challenge.integrator.dto.basket.BasketDto;
import com.iyzico.challenge.integrator.dto.basket.BasketProductDto;
import com.iyzico.challenge.integrator.dto.product.ProductDto;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collections;

public class BasketMapperTest {
    @Tested
    private BasketMapper tested;

    private ProductMapper productMapper = new ProductMapper();

    @Before
    public void setup() {
        tested = new BasketMapper(productMapper);
    }

    @Test
    public void map() {
        Basket basket = new Basket();
        Product product = createProduct(1, "test", "barcode", 10, BigDecimal.ONE);

        BasketProduct basketProduct = new BasketProduct();
        basketProduct.setCount(1);
        basketProduct.setProduct(product);
        basketProduct.setBasket(basket);
        basket.setProducts(Collections.singleton(basketProduct));

        BasketDto result = tested.map(basket);
        Assert.assertNotNull(result);
        Assert.assertEquals(BigDecimal.ONE, result.getTotal());
        Assert.assertNotNull(result.getProducts());
        Assert.assertEquals(1, result.getProducts().size());
        BasketProductDto resultBasketProduct = result.getProducts().iterator().next();
        Assert.assertEquals(basketProduct.getCount(), resultBasketProduct.getCount());
        Assert.assertEquals(basketProduct.getSubtotal(), resultBasketProduct.getSubtotal());
        ProductDto resultProduct = resultBasketProduct.getProduct();
        Assert.assertNotNull(resultProduct);
        Assert.assertEquals(1, resultProduct.getId());
        Assert.assertEquals(product.getBarcode(), resultProduct.getBarcode());
        Assert.assertEquals(product.getName(), resultProduct.getName());
        Assert.assertEquals(10, resultProduct.getStockCount());
        Assert.assertEquals(BigDecimal.ONE, resultProduct.getPrice());
    }

    private Product createProduct(long id, String name, String barcode, long stockCount, BigDecimal price) {
        Product product = new Product();
        product.setId(id);
        product.setStockCount(stockCount);
        product.setBarcode(barcode);
        product.setStatus(stockCount > 0 ? Product.Status.IN_STOCK : Product.Status.OUT_OF_STOCK);
        product.setPrice(price);
        product.setName(name);
        return product;
    }
}