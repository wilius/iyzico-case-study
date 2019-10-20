package com.iyzico.challenge.integrator.data.entity;

import mockit.StrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;

@RunWith(JMockit.class)
public class PaymentProductTest {
    @Tested
    private PaymentProduct tested;

    @Test
    public void mapping_test() {
        long id = 1;
        long paymentId = 2;
        long productId = 3;
        int count = 4;
        BigDecimal price = BigDecimal.ONE;

        UserPayment payment = new UserPayment();
        Product product = new Product();

        new StrictExpectations() {{
        }};

        tested.setId(id);
        tested.setPaymentId(paymentId);
        tested.setProductId(productId);
        tested.setCount(count);
        tested.setPrice(price);
        tested.setProduct(product);
        tested.setPayment(payment);

        Assert.assertEquals(id, tested.getId());
        Assert.assertEquals(paymentId, tested.getPaymentId());
        Assert.assertEquals(productId, tested.getProductId());
        Assert.assertEquals(count, tested.getCount());
        Assert.assertEquals(price, tested.getPrice());
        Assert.assertEquals(product, tested.getProduct());
        Assert.assertEquals(payment, tested.getPayment());
    }
}