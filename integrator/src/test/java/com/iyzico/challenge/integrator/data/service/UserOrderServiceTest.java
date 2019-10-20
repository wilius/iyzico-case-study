package com.iyzico.challenge.integrator.data.service;

import com.iyzico.challenge.integrator.data.entity.Basket;
import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.data.entity.UserOrder;
import com.iyzico.challenge.integrator.data.entity.UserPayment;
import com.iyzico.challenge.integrator.data.repository.UserOrderRepository;
import mockit.Injectable;
import mockit.Mocked;
import mockit.StrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class UserOrderServiceTest {
    @Tested
    private UserOrderService tested;

    @Injectable
    private UserOrderRepository repository;

    @Test
    public void create(@Mocked User user,
                       @Mocked UserPayment payment,
                       @Mocked Basket basket) {
        new StrictExpectations() {{
        }};
        UserOrder result = tested.create(user, payment, basket);
        Assert.assertEquals(user, result.getUser());
        Assert.assertEquals(payment, result.getPayment());
        Assert.assertEquals(basket, result.getBasket());

    }
}