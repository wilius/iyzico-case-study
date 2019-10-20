package com.iyzico.challenge.integrator.data.entity;

import mockit.StrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;

@RunWith(JMockit.class)
public class BasketProductTest {
    @Tested
    private BasketProduct tested;

    @Test
    public void mapping_test() {
        long id = 1;
        long basketId = 2;
        long productId = 3;
        int count = 4;
        LocalDateTime createTime = LocalDateTime.now();
        Basket basket = new Basket();
        Product product = new Product();

        new StrictExpectations() {{
        }};

        tested.setId(id);
        tested.setCount(count);
        tested.setBasket(basket);
        tested.setProduct(product);
        tested.setBasketId(basketId);
        tested.setProductId(productId);
        tested.setCreateTime(createTime);

        Assert.assertEquals(id, tested.getId());
        Assert.assertEquals(basketId, tested.getBasketId());
        Assert.assertEquals(productId, tested.getProductId());
        Assert.assertEquals(count, tested.getCount());
        Assert.assertEquals(createTime, tested.getCreateTime());
        Assert.assertEquals(basket, tested.getBasket());
        Assert.assertEquals(product, tested.getProduct());
    }
}