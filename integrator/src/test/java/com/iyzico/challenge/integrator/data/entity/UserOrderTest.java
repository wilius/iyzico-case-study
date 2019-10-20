package com.iyzico.challenge.integrator.data.entity;

import mockit.StrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;

@RunWith(JMockit.class)
public class UserOrderTest {
    @Tested
    private UserOrder tested;

    @Test
    public void mapping_test() {
        long id = 1;
        long userId = 2;
        long paymentId = 3;
        long basketId = 4;
        LocalDateTime createTime = LocalDateTime.now();

        User user = new User();
        UserPayment payment = new UserPayment();
        Basket basket = new Basket();

        new StrictExpectations() {{
        }};

        tested.setId(id);
        tested.setCreateTime(createTime);
        tested.setUser(user);
        tested.setBasket(basket);
        tested.setPayment(payment);
        tested.setBasketId(basketId);
        tested.setPaymentId(paymentId);
        tested.setUserId(userId);

        Assert.assertEquals(id, tested.getId());
        Assert.assertEquals(createTime, tested.getCreateTime());
        Assert.assertEquals(user, tested.getUser());
        Assert.assertEquals(basket, tested.getBasket());
        Assert.assertEquals(payment, tested.getPayment());
        Assert.assertEquals(basketId, tested.getBasketId());
        Assert.assertEquals(paymentId, tested.getPaymentId());
        Assert.assertEquals(userId, tested.getUserId());
    }
}