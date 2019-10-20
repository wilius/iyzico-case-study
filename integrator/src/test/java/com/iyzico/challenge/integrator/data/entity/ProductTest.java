package com.iyzico.challenge.integrator.data.entity;

import mockit.StrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RunWith(JMockit.class)
public class ProductTest {
    @Tested
    private Product tested;

    @Test
    public void mapping_test() {
        long id = 1;
        String name = "name";
        long userId = 2;
        long stockCount = 3;
        Product.Status status = Product.Status.IN_STOCK;
        BigDecimal price = BigDecimal.ONE;
        String barcode = "barcode";
        User user = new User();
        LongText description = new LongText();

        new StrictExpectations() {{
        }};

        tested.setId(id);
        tested.setStockCount(stockCount);
        tested.setBarcode(barcode);
        tested.setStatus(status);
        tested.setDescription(description);
        tested.setName(name);
        tested.setPrice(price);
        tested.setUser(user);
        tested.setUserId(userId);

        Assert.assertEquals(id, tested.getId());
        Assert.assertEquals(name, tested.getName());
        Assert.assertEquals(userId, tested.getUserId());
        Assert.assertEquals(stockCount, tested.getStockCount());
        Assert.assertEquals(status, tested.getStatus());
        Assert.assertEquals(price, tested.getPrice());
        Assert.assertEquals(barcode, tested.getBarcode());
        Assert.assertEquals(userId, tested.getUserId());
        Assert.assertEquals(description, tested.getDescription());
    }
}